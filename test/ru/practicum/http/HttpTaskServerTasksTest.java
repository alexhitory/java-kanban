package ru.practicum.http;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;

import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerTasksTest extends HttpTaskServerTestBase {
    @Test
    void shouldCreateAndGetTask() throws Exception {
        Task task = new Task("Task", "Description", Status.NEW);

        HttpResponse<String> createResponse = post("/tasks", gson.toJson(task));
        HttpResponse<String> getResponse = get("/tasks/1");

        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.statusCode());
        assertEquals(HttpURLConnection.HTTP_OK, getResponse.statusCode());
        assertEquals("Task", manager.getTaskById(1).getTitle());
    }

    @Test
    void shouldReturnNotFoundForUnknownTask() throws Exception {
        HttpResponse<String> response = get("/tasks/999");

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void shouldReturnNotAcceptableForIntersectingTask() throws Exception {
        Task first = new Task("First", "Description", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0));
        Task second = new Task("Second", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 30));

        post("/tasks", gson.toJson(first));
        HttpResponse<String> response = post("/tasks", gson.toJson(second));

        assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, response.statusCode());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        manager.createTask(new Task("Task", "Description", Status.NEW));

        HttpResponse<String> response = delete("/tasks/1");

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }
}
