package org.example;

import io.javalin.Javalin;
import org.example.api.MovieApi;
import org.example.api.UserApi;
import org.example.cli.MovieCLI;
import org.example.repository.MovieRepository;
import org.example.repository.UserRepository;
import org.example.service.MovieService;
import org.example.service.UserService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            MovieRepository movieRepository = new MovieRepository("movies.json");
            MovieService    movieService    = new MovieService(movieRepository);

            if (args.length > 0 && args[0].equals("--cli")) {
                new MovieCLI(movieService).run();
            } else {
                Javalin app = new MovieApi(movieService).createApp();

                UserRepository userRepository = new UserRepository("users.json");
                UserService    userService    = new UserService(userRepository);
                new UserApi(userService).registerRoutes(app);

                app.start(7071);
                System.out.println("API ready → http://localhost:7071");
                System.out.println("Open frontend/index.html in your browser");
            }
        } catch (IOException e) {
            System.err.println("启动失败：" + e.getMessage());
        }
    }
}
