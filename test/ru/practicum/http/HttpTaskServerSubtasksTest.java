package ru.practicum.http;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.Subtask;

import java.net.http.HttpResponse;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerSubtasksTest extends HttpTaskServerTestBase {
    @Test
    void shouldCreateAndGetSubtask() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());

        HttpResponse<String> createResponse = post("/subtasks", gson.toJson(subtask));
        HttpResponse<String> getResponse = get("/subtasks/2");

        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.statusCode());
        assertEquals(HttpURLConnection.HTTP_OK, getResponse.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    void shouldReturnNotFoundForSubtaskWithoutEpic() throws Exception {
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 999);

        HttpResponse<String> response = post("/subtasks", gson.toJson(subtask));

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void shouldDeleteSubtask() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        HttpResponse<String> response = delete("/subtasks/" + subtask.getId());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(0, manager.getAllSubtasks().size());
    }
}
