package ru.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TaskIntersectionException;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            process(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TaskIntersectionException e) {
            sendHasInteractions(exchange);
        } catch (JsonSyntaxException | IllegalArgumentException e) {
            sendResponse(exchange, 500, "{\"error\":\"Invalid request\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    protected abstract void process(HttpExchange exchange) throws IOException;

    protected <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(body, type);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, 200, text);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, 201, text);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 404, "{\"error\":\"" + message + "\"}");
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 406, "{\"error\":\"Task time intersects with existing task\"}");
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected Integer extractId(HttpExchange exchange, String basePath) {
        String path = exchange.getRequestURI().getPath();
        if (path.equals(basePath)) {
            return null;
        }

        String prefix = basePath + "/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            throw new NotFoundException("Resource not found");
        }

        String idPart = path.substring(prefix.length());
        if (idPart.contains("/")) {
            throw new NotFoundException("Resource not found");
        }

        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Resource not found");
        }
    }

    protected boolean hasIntersection(Task task) {
        if (task == null || task.getStartTime() == null) {
            return false;
        }

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        return prioritizedTasks.stream()
                .anyMatch(existing -> existing.getId() != task.getId()
                        && existing.getStartTime().isBefore(task.getEndTime())
                        && existing.getEndTime().isAfter(task.getStartTime()));
    }
}
