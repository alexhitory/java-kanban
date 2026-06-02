package ru.practicum.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.TaskIntersectionException;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    private static final String PATH = "/tasks";

    public TasksHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Integer id = extractId(exchange, PATH);

        switch (method) {
            case "GET":
                if (id == null) {
                    sendText(exchange, gson.toJson(manager.getAllTasks()));
                    return;
                }
                Task task = manager.getTaskById(id);
                if (task == null) {
                    throw new NotFoundException("Task not found");
                }
                sendText(exchange, gson.toJson(task));
                break;
            case "POST":
                Task postedTask = readJson(exchange, Task.class);
                if (postedTask.getId() == 0) {
                    if (hasIntersection(postedTask)) {
                        throw new TaskIntersectionException("Task time intersects");
                    }
                    Task created = manager.createTask(postedTask);
                    if (created == null) {
                        throw new TaskIntersectionException("Task time intersects");
                    }
                    sendCreated(exchange, gson.toJson(created));
                    return;
                }
                if (manager.getTaskById(postedTask.getId()) == null) {
                    throw new NotFoundException("Task not found");
                }
                if (hasIntersection(postedTask)) {
                    throw new TaskIntersectionException("Task time intersects");
                }
                manager.updateTask(postedTask);
                sendCreated(exchange, gson.toJson(postedTask));
                break;
            case "DELETE":
                if (id == null) {
                    manager.removeAllTasks();
                    sendCreated(exchange, "{}");
                    return;
                }
                if (manager.getTaskById(id) == null) {
                    throw new NotFoundException("Task not found");
                }
                manager.removeTaskById(id);
                sendCreated(exchange, "{}");
                break;
            default:
                throw new NotFoundException("Resource not found");
        }
    }
}
