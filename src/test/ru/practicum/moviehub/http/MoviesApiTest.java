package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";

    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;
    private static final Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(
                req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(200, resp.statusCode());

        List<Movie> movies = gson.fromJson(
                resp.body(),
                new ListOfMoviesTypeToken().getType()
        );

        assertTrue(movies.isEmpty());
    }

    @Test
    void postMovie_thenGetAll() throws Exception {

        String json = "{\"title\":\"Inception\",\"year\":2010}";

        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(post, HttpResponse.BodyHandlers.ofString());

        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(get, HttpResponse.BodyHandlers.ofString());

        List<Movie> movies = gson.fromJson(
                resp.body(),
                new ListOfMoviesTypeToken().getType()
        );

        assertEquals(1, movies.size());
        assertEquals("Inception", movies.get(0).getTitle());
    }
}