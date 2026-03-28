package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        try {

            if (path.matches("/movies/\\d+")) {
                handleById(ex, method, path);
                return;
            }

            if (path.equals("/movies")) {

                if ("GET".equalsIgnoreCase(method)) {
                    handleGetAll(ex, query);

                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(ex);

                } else {
                    sendError(ex, 405, "Метод не поддерживается"); // ❗ FIX
                }

                return;
            }

            sendError(ex, 404, "Не найдено");

        } finally {
            ex.close();
        }
    }

    private void handleGetAll(HttpExchange ex, String query) throws IOException {

        if (query != null && query.startsWith("year=")) {
            try {
                int year = Integer.parseInt(query.substring(5));
                sendJson(ex, 200, store.getByYear(year));
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Некорректный параметр запроса — 'year'");
            }
            return;
        }

        sendJson(ex, 200, store.getAll());
    }

    private void handlePost(HttpExchange ex) throws IOException {

        String contentType = ex.getRequestHeaders().getFirst("Content-Type");

        if (contentType == null || !contentType.contains("application/json")) {
            sendError(ex, 415, "Неподдерживаемый Content-Type");
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Movie req;
        try {
            req = gson.fromJson(body, Movie.class);
        } catch (Exception e) {
            sendJson(ex, 422,
                    new ErrorResponse("Ошибка валидации", List.of("Некорректный JSON")));
            return;
        }

        List<String> errors = validate(req);

        if (!errors.isEmpty()) {
            sendJson(ex, 422, new ErrorResponse("Ошибка валидации", errors));
            return;
        }

        Movie created = store.add(req.getTitle(), req.getYear());
        sendJson(ex, 201, created);
    }

    private List<String> validate(Movie m) {

        List<String> errors = new ArrayList<>();

        if (m.getTitle() == null || m.getTitle().isBlank()) {
            errors.add("название не должно быть пустым");

        } else if (m.getTitle().length() > 100) {
            errors.add("название слишком длинное");
        }

        int currentYear = Year.now().getValue();

        if (m.getYear() < 1888 || m.getYear() > currentYear + 1) {
            errors.add("год должен быть между 1888 и " + (currentYear + 1));
        }

        return errors;
    }

    private void handleById(HttpExchange ex, String method, String path) throws IOException {

        String idStr = path.substring("/movies/".length());

        int id;

        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendError(ex, 400, "Некорректный ID");
            return;
        }

        if ("GET".equalsIgnoreCase(method)) {

            Movie movie = store.getById(id);

            if (movie == null) {
                sendError(ex, 404, "Фильм не найден");
            } else {
                sendJson(ex, 200, movie);
            }

        } else if ("DELETE".equalsIgnoreCase(method)) {

            boolean deleted = store.delete(id);

            if (deleted) {
                sendNoContent(ex);
            } else {
                sendError(ex, 404, "Фильм не найден");
            }

        } else {
            sendError(ex, 405, "Метод не поддерживается");
        }
    }
}