# CCPractice 项目架构笔记

> 这份文档记录了这个电影评分项目的完整架构讲解，适合回顾复习。

---

## 项目结构一览

```
CCPractice/
├── frontend/
│   └── index.html              # 前端页面（用户看到的界面）
├── src/main/java/org/example/
│   ├── model/
│   │   └── Movie.java          # 数据模型（一部电影长什么样）
│   ├── repository/
│   │   └── MovieRepository.java # 负责读写 movies.json 文件
│   ├── service/
│   │   └── MovieService.java   # 业务逻辑（排序、筛选、校验）
│   ├── api/
│   │   ├── MovieApi.java       # HTTP 接口（接收前端请求）
│   │   └── MovieRequest.java   # 新增电影时前端发来的数据格式
│   └── Main.java               # 程序入口
├── src/test/                   # 单元测试
└── movies.json                 # 数据存储文件
```

---

## 一个请求的完整旅程

> 场景：用户打开页面，点击"查看电影列表"

```
用户打开 index.html
       ↓
[第1层] 前端 JavaScript 自动执行 loadAll()
       ↓  发出 HTTP 请求
[网络] GET http://localhost:7071/movies
       ↓  请求到达后端
[第2层] Javalin/Jetty 监听 7071 端口，接住请求
       ↓  匹配路由
[第3层] MovieApi.java — 认出是 GET /movies，取出参数
       ↓  调用方法
[第4层] MovieService.java — 从 cache 里取数据、按规则处理
       ↓  启动时加载一次
[第5层] MovieRepository.java — 读取 movies.json，转成 Java 对象
       ↓
[存储] movies.json — 数据的最终家园
```

返回方向反过来，一层层把数据传回给前端显示。

---

## 每一层详解

### 第一层：前端 `frontend/index.html`

**职责：** 用户看到的界面，负责展示数据、接收用户输入。

**核心代码：**
```javascript
// 页面加载时自动拉取数据
async function loadAll() {
    const res = await fetch('http://localhost:7071/movies'); // 发 HTTP 请求
    allMovies = await res.json();  // 解析返回的 JSON
    render(allMovies);             // 把电影画到页面上
}
```

**fetch 的两个方向：**

| 时机 | 方向 | 目的 |
|------|------|------|
| 页面加载 / 刷新列表 | 后端 → 前端（拉数据） | 显示已有电影 |
| 点了 Add Movie | 前端 → 后端（推数据） | 把新电影发给后端保存 |

**如果去掉：** 数据还在，但用户根本无法使用，只能靠命令行。

---

### 第二层：HTTP 协议（网络传输）

**职责：** 前端（浏览器）和后端（Java程序）之间的通信语言。

**请求长什么样：**
```
GET /movies HTTP/1.1          ← 查询电影列表
Host: localhost:7071

POST /movies HTTP/1.1         ← 新增电影
{"title":"Inception","genre":"Sci-Fi","rating":9.0}
```

- **GET** = 取数据（只看，不改）
- **POST** = 提交数据（新增）

**Javalin 的作用：** 帮我们在 7071 端口"开一扇门"守着，你在控制台看到的这行就是证明：
```
Listening on http://localhost:7071/
```

**如果去掉：** 前端和后端没有共同语言，根本没法通信。

---

### 第三层：`MovieApi.java` — 请求的"门卫"

**职责：** 认路（判断请求类型和地址）+ 翻译（取出参数、把结果转成JSON）。

**核心代码：**
```java
// 注册三条"路"
app.get("/movies", ctx -> { ... });        // 查全部 / 按类型筛选
app.post("/movies", ctx -> { ... });       // 新增电影
app.get("/movies/top", ctx -> { ... });    // 查 Top-N
```

**它不做业务逻辑，只做转发：**
```java
app.get("/movies", ctx -> {
    String genre = ctx.queryParam("genre"); // 取出参数
    List<Movie> movies = service.getAllMovies(); // 转手给 Service
    ctx.json(movies); // 把结果转成 JSON 返回
});
```

**如果去掉：** 请求进来不知道该交给谁，直接 404 报错。就像大楼没有门卫和指示牌。

---

### 第四层：`MovieService.java` — 真正干活的人

**职责：** 所有业务规则都在这里，和 HTTP、文件存储无关。

**核心代码：**
```java
// 查 Top-N：业务规则在这里
public List<Movie> getTopN(int n) {
    if (n <= 0) throw new IllegalArgumentException("n must be positive");
    return cache.stream()
            .sorted(Comparator.comparingDouble(Movie::getRating).reversed())
            .limit(n)
            .collect(Collectors.toList());
}
```

**重要：cache 机制**

程序启动时，一次性把所有电影从文件读进内存：
```java
this.cache = repository.loadAll(); // 启动时执行一次
```

之后所有查询都在内存里操作，速度快。只有 `addMovie` 时才会写文件：
```java
cache.add(movie);           // 先加到内存
repository.saveAll(cache);  // 再同步写入文件
```

**cache 的局限性：** 数据量很大时会撑爆内存。真实项目用数据库解决这个问题，数据库每次只取需要的数据，不会全部加载进内存。

**如果去掉：** API 和 CLI 各自要写一套排序、校验逻辑，重复代码且难以维护。

---

### 第五层：`MovieRepository.java` + `movies.json`

**职责：** 只负责"数据怎么存、怎么取"，和业务逻辑无关。

**核心代码：**
```java
// 从文件读出来，变成 Java 对象列表
public List<Movie> loadAll() throws IOException {
    return mapper.readValue(dataFile, new TypeReference<List<Movie>>() {});
}

// 把 Java 对象列表，写回文件
public void saveAll(List<Movie> movies) throws IOException {
    mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, movies);
}
```

**Jackson 的作用：**
```
Java 对象  →  JSON 文本   （写文件，saveAll）
JSON 文本  →  Java 对象   （读文件，loadAll）
```

**如果去掉：** 把文件读写代码混进 Service，将来换数据库时，业务逻辑和存储代码搅在一起，很容易改出 bug。

---

## 为什么要分层？

### 问题一：改一个地方，容易破坏另一个地方

评分校验只在 `Movie.java` 构造方法里的**一个地方**：
```java
if (rating < 1.0 || rating > 10.0) {
    throw new IllegalArgumentException("Rating must be between 1 and 10");
}
```
改一行，API 和 CLI 全部生效。不分层的话，这个逻辑会散落在各处，漏改一处就是 bug。

### 问题二：没办法单独测试

`MovieServiceTest.java` 可以完全不启动 HTTP 服务，直接测业务逻辑：
```java
Movie m = service.addMovie("Inception", "Sci-Fi", 9.0);
assertEquals("Inception", m.getTitle()); // 直接验证结果
```
不分层的话，测试必须把整个程序跑起来，又慢又复杂。

### 问题三：换存储方式代价极小

将来换成 MySQL 数据库，只需要改 `MovieRepository.java` 一个文件。
`MovieService`、`MovieApi`、前端——全部不用动。
因为它们只认识 `repository.loadAll()` 和 `repository.saveAll()`，不关心背后是文件还是数据库。

### 一句话总结

> **每一层只负责一件事，只和相邻的层说话。变化被控制在最小范围内。**
>
> 这叫"关注点分离"，是软件工程最重要的思想之一。

---

## 三个 HTTP 接口速查

| 方法 | 地址 | 作用 | 示例 |
|------|------|------|------|
| GET | `/movies` | 查全部电影 | `GET /movies` |
| GET | `/movies?genre=Action` | 按类型筛选 | `GET /movies?genre=Sci-Fi` |
| GET | `/movies/top?n=5` | 查 Top-N | `GET /movies/top?n=3` |
| POST | `/movies` | 新增电影 | body: `{"title":"...","genre":"...","rating":9.0}` |

---

## 启动方式

```
# API 模式（默认），然后用浏览器打开 frontend/index.html
运行 Main.java

# 命令行模式
运行 Main.java，并传入参数 --cli
```
