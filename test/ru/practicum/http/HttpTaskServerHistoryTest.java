package ru.practicum.http;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerHistoryTest extends HttpTaskServerTestBase {
    @Test
    void shouldGetHistory() throws Exception {
        Task task = manager.createTask(new Task("Task", "Description", Status.NEW));
        manager.getTaskById(task.getId());

        HttpResponse<String> response = get("/history");

        assertEquals(200, response.statusCode());
        assertEquals(1, manager.getHistory().size());
    }

    @Test
    void shouldReturnNotFoundForInvalidHistoryPath() throws Exception {
        HttpResponse<String> response = get("/history/1");

        assertEquals(404, response.statusCode());
    }
}
