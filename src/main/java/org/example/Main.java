package org.example;

import org.example.api.MovieApi;
import org.example.cli.MovieCLI;
import org.example.repository.MovieRepository;
import org.example.service.MovieService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String dataFile = "movies.json";
        try {
            MovieRepository repository = new MovieRepository(dataFile);
            MovieService    service    = new MovieService(repository);

            if (args.length > 0 && args[0].equals("--cli")) {
                new MovieCLI(service).run();
            } else {
                new MovieApi(service).createApp().start(7071);
                System.out.println("API ready → http://localhost:7071");
                System.out.println("Open frontend/index.html in your browser");
            }
        } catch (IOException e) {
            System.err.println("启动失败：" + e.getMessage());
        }
    }
}
