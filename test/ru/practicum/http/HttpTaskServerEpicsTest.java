package ru.practicum.http;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.Subtask;

import java.net.http.HttpResponse;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerEpicsTest extends HttpTaskServerTestBase {
    @Test
    void shouldCreateAndGetEpic() throws Exception {
        Epic epic = new Epic("Epic", "Description");

        HttpResponse<String> createResponse = post("/epics", gson.toJson(epic));
        HttpResponse<String> getResponse = get("/epics/1");

        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.statusCode());
        assertEquals(HttpURLConnection.HTTP_OK, getResponse.statusCode());
        assertEquals("Epic", manager.getEpicById(1).getTitle());
    }

    @Test
    void shouldGetEpicSubtasks() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        HttpResponse<String> response = get("/epics/" + epic.getId() + "/subtasks");

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(1, manager.getSubtasksOfEpic(epic.getId()).size());
    }

    @Test
    void shouldReturnNotFoundForUnknownEpic() throws Exception {
        HttpResponse<String> response = get("/epics/999");

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        HttpResponse<String> response = delete("/epics/" + epic.getId());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubtasks().size());
    }
}
