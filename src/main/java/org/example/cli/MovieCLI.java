package org.example.cli;

import org.example.model.Movie;
import org.example.service.MovieService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MovieCLI {

    private final MovieService service;
    private final Scanner scanner;

    public MovieCLI(MovieService service) {
        this.service = service;
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    public void run() {
        System.out.println("=== 电影评分工具 ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": handleAdd();           break;
                case "2": handleFilterByGenre(); break;
                case "3": handleTopN();          break;
                case "4": handleListAll();       break;
                case "0": running = false;        break;
                default:  System.out.println("无效选项，请重试。");
            }
        }
        System.out.println("再见！");
    }

    private void printMenu() {
        System.out.println("\n1. 添加电影");
        System.out.println("2. 按类型筛选");
        System.out.println("3. 查看 Top-N");
        System.out.println("4. 查看全部");
        System.out.println("0. 退出");
        System.out.print("请选择：");
    }

    private void handleAdd() {
        System.out.print("标题：");
        String title = scanner.nextLine().trim();

        System.out.print("类型（如 Action / Comedy / Drama）：");
        String genre = scanner.nextLine().trim();

        double rating = 0;
        while (true) {
            System.out.print("评分（1-10）：");
            try {
                rating = Double.parseDouble(scanner.nextLine().trim());
                if (rating < 1.0 || rating > 10.0) {
                    System.out.println("评分必须在 1~10 之间。");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字。");
            }
        }

        try {
            Movie m = service.addMovie(title, genre, rating);
            System.out.println("已添加：" + m);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("添加失败：" + e.getMessage());
        }
    }

    private void handleFilterByGenre() {
        System.out.print("输入类型：");
        String genre = scanner.nextLine().trim();
        List<Movie> result = service.filterByGenre(genre);
        if (result.isEmpty()) {
            System.out.println("没有找到该类型的电影。");
        } else {
            result.forEach(System.out::println);
        }
    }

    private void handleTopN() {
        System.out.print("输入 N：");
        try {
            int n = Integer.parseInt(scanner.nextLine().trim());
            List<Movie> top = service.getTopN(n);
            if (top.isEmpty()) {
                System.out.println("暂无电影数据。");
            } else {
                System.out.println("--- Top " + n + " ---");
                top.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入有效整数。");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleListAll() {
        List<Movie> all = service.getAllMovies();
        if (all.isEmpty()) {
            System.out.println("暂无电影数据。");
        } else {
            all.forEach(System.out::println);
        }
    }
}
