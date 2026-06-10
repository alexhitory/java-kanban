package ru.practicum.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.exception.NotFoundException;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    private static final String PATH = "/epics";

    public EpicsHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/subtasks")) {
            handleEpicSubtasks(exchange);
            return;
        }

        String method = exchange.getRequestMethod();
        Integer id = extractId(exchange, PATH);

        switch (method) {
            case "GET":
                if (id == null) {
                    sendText(exchange, gson.toJson(manager.getAllEpics()));
                    return;
                }
                Epic epic = manager.getEpicById(id);
                if (epic == null) {
                    throw new NotFoundException("Epic not found");
                }
                sendText(exchange, gson.toJson(epic));
                break;
            case "POST":
                Epic postedEpic = readJson(exchange, Epic.class);
                if (postedEpic.getId() == 0) {
                    sendCreated(exchange, gson.toJson(manager.createEpic(postedEpic)));
                    return;
                }
                if (manager.getEpicById(postedEpic.getId()) == null) {
                    throw new NotFoundException("Epic not found");
                }
                manager.updateEpic(postedEpic);
                sendCreated(exchange, gson.toJson(postedEpic));
                break;
            case "DELETE":
                if (id == null) {
                    manager.removeAllEpics();
                    sendCreated(exchange, "{}");
                    return;
                }
                if (manager.getEpicById(id) == null) {
                    throw new NotFoundException("Epic not found");
                }
                manager.removeEpicById(id);
                sendCreated(exchange, "{}");
                break;
            default:
                throw new NotFoundException("Resource not found");
        }
    }

    private void handleEpicSubtasks(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            throw new NotFoundException("Resource not found");
        }

        String path = exchange.getRequestURI().getPath();
        String prefix = PATH + "/";
        String suffix = "/subtasks";
        String idPart = path.substring(prefix.length(), path.length() - suffix.length());

        try {
            int epicId = Integer.parseInt(idPart);
            if (manager.getEpicById(epicId) == null) {
                throw new NotFoundException("Epic not found");
            }
            sendText(exchange, gson.toJson(manager.getSubtasksOfEpic(epicId)));
        } catch (NumberFormatException e) {
            throw new NotFoundException("Resource not found");
        }
    }
}
