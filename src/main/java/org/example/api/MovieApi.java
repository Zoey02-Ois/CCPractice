package org.example.api;

import io.javalin.Javalin;
import org.example.model.Movie;
import org.example.service.MovieService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MovieApi {

    private final MovieService service;

    public MovieApi(MovieService service) {
        this.service = service;
    }// 依赖注入

    public Javalin createApp() { //javalin是返回值类型
        Javalin app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
            config.showJavalinBanner = false;
        });

        // GET /movies?genre=Action  (genre 可选)
        // http://localhost:7070/movies?genre=Action&year=2023 找到这个地址下2023年的动作片
        // 问号的前面是目标地址 (Path)问号本身：语气的转折点
        // ?这个符号在网址里的官方名字叫“查询字符串分隔符”。一旦服务器看到了 ?，它就知道：地址已经找完了，接下来顾客要开始提“附加条件”了。
        // 问号的后面：查询参数 (Query Parameters) genre=Action 这部分叫做“键值对”（Key-Value Pair），genre 是键（参数名），Action 是值（具体内容）。它告诉服务器你要怎么筛选数据。
        app.get("/movies", ctx -> {
            // 类型推断：IDE 怎么知道 ctx 是 Context？
            // app.get()，的 Javalin 类的源码，ide发现这个方法的标准定义是：get(String path, Handler handler)。
            // 编译器发现第二个参数必须是一个 Handler。它又去查 Handler 的源码，发现这是一个接口，里面只有一个方法：void handle(Context ctx)。
            // 既然你把 ctx -> {...} 放在了第二个参数的位置，那它肯定是在实现 Handler 接口。既然 Handler 接口的方法规定了接收的参数必须是 Context 类型，那你写的这个变量 ctx，绝对、只能、必定是 Context 类型！
            // 而不用new一个context
            String genre = ctx.queryParam("genre");
            List<Movie> movies = (genre != null && !genre.isEmpty())
                    ? service.filterByGenre(genre) // 三元运算符。if-else语句的压缩包，？是条件成立时，：是条件不成立时，最终结果都会被塞进list movie这个列表变量
                    : service.getAllMovies();
            ctx.json(movies);
        });

        // POST /movies  body: { title, genre, rating }
        app.post("/movies", ctx -> {
            MovieRequest req = ctx.bodyAsClass(MovieRequest.class);
            if (req.getTitle() == null || req.getGenre() == null) {
                ctx.status(400).json(Collections.singletonMap("error", "title and genre are required"));
                return;
            }
            Movie movie = service.addMovie(req.getTitle(), req.getGenre(), req.getRating());
            ctx.status(201).json(movie);
        });

        // GET /movies/top?n=5
        app.get("/movies/top", ctx -> {
            String nParam = ctx.queryParam("n");
            int n;
            try {
                n = (nParam != null && !nParam.isEmpty()) ? Integer.parseInt(nParam) : 5;
            } catch (NumberFormatException e) {
                ctx.status(400).json(Collections.singletonMap("error", "n must be a positive integer"));
                return;
            }
            List<Movie> top = service.getTopN(n);
            ctx.json(top);
        });

        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(400).json(Collections.singletonMap("error", e.getMessage())));

        app.exception(IOException.class, (e, ctx) ->
                ctx.status(500).json(Collections.singletonMap("error", "Storage error: " + e.getMessage())));

        return app;
    }
}
