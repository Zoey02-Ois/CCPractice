package org.example.api;

import io.javalin.Javalin;
import org.example.model.User;
import org.example.service.UserService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserApi {

    private final UserService service;

    public UserApi(UserService service) {
        this.service = service;
    }

    /**
     * 将用户相关路由注册到已有的 Javalin 实例上，与 MovieApi 共用同一个端口。
     *
     * POST /api/register  →  201 Created | 400 Bad Request | 409 Conflict
     * POST /api/login     →  200 OK      | 400 Bad Request | 401 Unauthorized
     */
    public void registerRoutes(Javalin app) {

        // POST /api/register
        app.post("/api/register", ctx -> {
            RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);

            if (req.getUsername() == null || req.getPassword() == null) {
                ctx.status(400).json(Collections.singletonMap("error", "username and password are required"));
                return;
            }

            if (service.existsByUsername(req.getUsername().trim())) {
                ctx.status(409).json(Collections.singletonMap("error", "Username already exists"));
                return;
            }

            User user = service.register(req.getUsername(), req.getPassword());

            // 响应中不返回密码哈希
            Map<String, String> body = new LinkedHashMap<>();
            body.put("username",  user.getUsername());
            body.put("createdAt", user.getCreatedAt());
            ctx.status(201).json(body);
        });

        // POST /api/login
        app.post("/api/login", ctx -> {
            LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

            if (req.getUsername() == null || req.getPassword() == null) {
                ctx.status(400).json(Collections.singletonMap("error", "username and password are required"));
                return;
            }

            boolean ok = service.login(req.getUsername(), req.getPassword());
            if (!ok) {
                ctx.status(401).json(Collections.singletonMap("error", "Invalid username or password"));
                return;
            }

            ctx.status(200).json(Collections.singletonMap("message", "Login successful"));
        });
    }
}
