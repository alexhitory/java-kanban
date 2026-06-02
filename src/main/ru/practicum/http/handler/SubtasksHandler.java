package ru.practicum.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TaskIntersectionException;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    private static final String PATH = "/subtasks";

    public SubtasksHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Integer id = extractId(exchange, PATH);

        switch (method) {
            case "GET":
                if (id == null) {
                    sendText(exchange, gson.toJson(manager.getAllSubtasks()));
                    return;
                }
                Subtask subtask = manager.getSubtaskById(id);
                if (subtask == null) {
                    throw new NotFoundException("Subtask not found");
                }
                sendText(exchange, gson.toJson(subtask));
                break;
            case "POST":
                Subtask postedSubtask = readJson(exchange, Subtask.class);
                if (manager.getEpicById(postedSubtask.getEpicId()) == null) {
                    throw new NotFoundException("Epic not found");
                }
                if (postedSubtask.getId() != 0 && manager.getSubtaskById(postedSubtask.getId()) == null) {
                    throw new NotFoundException("Subtask not found");
                }
                if (hasIntersection(postedSubtask)) {
                    throw new TaskIntersectionException("Task time intersects");
                }
                if (postedSubtask.getId() == 0) {
                    Subtask created = manager.createSubtask(postedSubtask);
                    if (created == null) {
                        throw new TaskIntersectionException("Task time intersects");
                    }
                    sendCreated(exchange, gson.toJson(created));
                    return;
                }
                manager.updateSubtask(postedSubtask);
                sendCreated(exchange, gson.toJson(postedSubtask));
                break;
            case "DELETE":
                if (id == null) {
                    manager.removeAllSubtasks();
                    sendCreated(exchange, "{}");
                    return;
                }
                if (manager.getSubtaskById(id) == null) {
                    throw new NotFoundException("Subtask not found");
                }
                manager.removeSubtaskById(id);
                sendCreated(exchange, "{}");
                break;
            default:
                throw new NotFoundException("Resource not found");
        }
    }
}
