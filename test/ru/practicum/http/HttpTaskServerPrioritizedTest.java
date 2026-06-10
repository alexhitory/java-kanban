package ru.practicum.http;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;

import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerPrioritizedTest extends HttpTaskServerTestBase {
    @Test
    void shouldGetPrioritizedTasks() throws Exception {
        manager.createTask(new Task("Late", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 12, 0)));
        manager.createTask(new Task("Early", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 0)));

        HttpResponse<String> response = get("/prioritized");

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(2, manager.getPrioritizedTasks().size());
        assertEquals("Early", manager.getPrioritizedTasks().get(0).getTitle());
    }

    @Test
    void shouldReturnNotFoundForInvalidPrioritizedPath() throws Exception {
        HttpResponse<String> response = get("/prioritized/1");

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }
}
