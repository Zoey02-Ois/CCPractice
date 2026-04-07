package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final File dataFile;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserRepository(String filePath) {
        this.dataFile = new File(filePath);
    }

    public List<User> loadAll() throws IOException {
        if (!dataFile.exists() || dataFile.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(dataFile, new TypeReference<List<User>>() {});
    }

    public void saveAll(List<User> users) throws IOException {
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, users);
    }

    /** 按用户名查找，找不到返回 empty。 */
    public Optional<User> findByUsername(String username) throws IOException {
        return loadAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /** 将用户追加写入文件（调用前请确认用户名不重复）。 */
    public void save(User user) throws IOException {
        List<User> all = loadAll();
        all.add(user);
        saveAll(all);
    }

    /** 检查用户名是否已存在。 */
    public boolean existsByUsername(String username) throws IOException {
        return loadAll().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }
}
