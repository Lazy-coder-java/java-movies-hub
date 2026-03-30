package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieByIdHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MovieByIdHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

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
