package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {

    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public Movie add(String title, int year) {
        Movie movie = new Movie(nextId++, title, year);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public Movie getById(int id) {
        return movies.get(id);
    }

    public boolean delete(int id) {
        return movies.remove(id) != null;
    }

    public List<Movie> getByYear(int year) {
        List<Movie> result = new ArrayList<>();
        for (Movie m : movies.values()) {
            if (m.getYear() == year) {
                result.add(m);
            }
        }
        return result;
    }

    public void clear() {
        movies.clear();
        nextId = 1;
    }
}