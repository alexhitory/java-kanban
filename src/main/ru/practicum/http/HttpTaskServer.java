package ru.practicum.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.http.adapter.DurationAdapter;
import ru.practicum.http.adapter.LocalDateTimeAdapter;
import ru.practicum.http.handler.EpicsHandler;
import ru.practicum.http.handler.HistoryHandler;
import ru.practicum.http.handler.PrioritizedHandler;
import ru.practicum.http.handler.SubtasksHandler;
import ru.practicum.http.handler.TasksHandler;
import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager, GSON));
        server.createContext("/subtasks", new SubtasksHandler(manager, GSON));
        server.createContext("/epics", new EpicsHandler(manager, GSON));
        server.createContext("/history", new HistoryHandler(manager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(manager, GSON));
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }

    public static Gson getGson() {
        return GSON;
    }

    public void start() {
        server.start();
        System.out.println("HTTP task server started on port " + PORT);
    }

    public void stop() {
        server.stop(0);
    }
}
