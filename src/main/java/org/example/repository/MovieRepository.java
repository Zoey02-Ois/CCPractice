package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Movie;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MovieRepository {

    private final File dataFile;
    private final ObjectMapper mapper = new ObjectMapper();

    public MovieRepository(String filePath) {
        this.dataFile = new File(filePath);
    }

    public List<Movie> loadAll() throws IOException {
        if (!dataFile.exists() || dataFile.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(dataFile, new TypeReference<List<Movie>>() {});
    }

    public void saveAll(List<Movie> movies) throws IOException {
        // 确保父目录存在
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, movies);
    }
}
