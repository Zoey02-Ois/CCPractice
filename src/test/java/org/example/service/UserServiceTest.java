package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @TempDir
    Path tempDir;

    private UserService service;

    @BeforeEach
    void setUp() throws IOException {
        String path = tempDir.resolve("users.json").toString();
        service = new UserService(new UserRepository(path));
    }

    // ── 注册 ──────────────────────────────────────────────────────────────────

    @Test
    void register_success_returnsUserWithCorrectUsername() throws IOException {
        User user = service.register("alice", "secret123");
        assertEquals("alice", user.getUsername());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void register_success_passwordIsHashed() throws IOException {
        User user = service.register("alice", "secret123");
        // 存储的密码不应与明文相同
        assertNotEquals("secret123", user.getPassword());
        // SHA-256 hex 固定 64 字符
        assertEquals(64, user.getPassword().length());
    }

    @Test
    void register_duplicateUsername_throwsIllegalStateException() throws IOException {
        service.register("alice", "secret123");
        assertThrows(IllegalStateException.class,
                () -> service.register("alice", "another123"));
    }

    @Test
    void register_usernameTooShort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("ab", "secret123"));
    }

    @Test
    void register_usernameTooLong_throwsIllegalArgumentException() {
        String longName = "a".repeat(21);
        assertThrows(IllegalArgumentException.class,
                () -> service.register(longName, "secret123"));
    }

    @Test
    void register_usernameWithSpecialChars_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("alice!", "secret123"));
    }

    @Test
    void register_passwordTooShort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("alice", "12345"));
    }

    // ── 登录 ──────────────────────────────────────────────────────────────────

    @Test
    void login_correctPassword_returnsTrue() throws IOException {
        service.register("bob", "mypassword");
        assertTrue(service.login("bob", "mypassword"));
    }

    @Test
    void login_wrongPassword_returnsFalse() throws IOException {
        service.register("bob", "mypassword");
        assertFalse(service.login("bob", "wrongpassword"));
    }

    @Test
    void login_nonExistentUser_returnsFalse() throws IOException {
        assertFalse(service.login("nobody", "anypassword"));
    }

    @Test
    void login_caseSensitiveUsername() throws IOException {
        service.register("carol", "pass1234");
        // 用户名大小写不同视为不同用户
        assertFalse(service.login("Carol", "pass1234"));
    }
}
