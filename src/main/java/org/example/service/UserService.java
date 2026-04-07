package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户注册 / 登录的业务规则：
 *
 * 用户名规则：
 *   - 长度 3 ~ 20 个字符
 *   - 只允许英文字母、数字、下划线（不允许空格或特殊符号）
 *
 * 密码规则：
 *   - 长度至少 6 个字符
 *   - 无字符集限制（支持任意可打印字符）
 *
 * 密码存储：
 *   - 使用 JDK 内置的 SHA-256（MessageDigest）做单向哈希后再持久化。
 *   - 取舍说明：SHA-256 不加盐，生产环境应使用 BCrypt/PBKDF2；
 *     此处为遵守"不引入第三方依赖"的约束而选择 JDK 原生方案。
 */
public class UserService {

    private static final int USERNAME_MIN = 3;
    private static final int USERNAME_MAX = 20;
    private static final int PASSWORD_MIN = 6;
    private static final String USERNAME_PATTERN = "[A-Za-z0-9_]+";

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * 注册新用户。
     *
     * @throws IllegalArgumentException 用户名 / 密码格式不合法
     * @throws IllegalStateException    用户名已存在
     * @throws IOException              存储层异常
     */
    public User register(String username, String password) throws IOException {
        validateUsername(username);
        validatePassword(password);

        if (repository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists: " + username);
        }

        String hashedPassword = hash(password);
        String createdAt = LocalDateTime.now().toString();
        User user = new User(username, hashedPassword, createdAt);
        repository.save(user);
        return user;
    }

    /**
     * 验证用户名和密码，返回登录是否成功。
     *
     * @throws IOException 存储层异常
     */
    public boolean login(String username, String password) throws IOException {
        Optional<User> found = repository.findByUsername(username);
        if (!found.isPresent()) {
            return false;
        }
        return found.get().getPassword().equals(hash(password));
    }

    /** 仅供 API 层做"用户名是否已存在"的快速判断（用于返回 409）。 */
    public boolean existsByUsername(String username) throws IOException {
        return repository.existsByUsername(username);
    }

    // ── 内部辅助 ──────────────────────────────────────────────────────────────

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        String trimmed = username.trim();
        if (trimmed.length() < USERNAME_MIN || trimmed.length() > USERNAME_MAX) {
            throw new IllegalArgumentException(
                    "Username must be between " + USERNAME_MIN + " and " + USERNAME_MAX + " characters");
        }
        if (!trimmed.matches(USERNAME_PATTERN)) {
            throw new IllegalArgumentException(
                    "Username may only contain letters, digits, and underscores");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (password.length() < PASSWORD_MIN) {
            throw new IllegalArgumentException(
                    "Password must be at least " + PASSWORD_MIN + " characters");
        }
    }

    /** SHA-256，返回十六进制字符串。 */
    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 强制要求实现的算法，不会走到这里
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
