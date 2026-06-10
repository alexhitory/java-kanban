package ru.practicum.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.exception.NotFoundException;
import ru.practicum.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod()) || !"/history".equals(exchange.getRequestURI().getPath())) {
            throw new NotFoundException("Resource not found");
        }
        sendText(exchange, gson.toJson(manager.getHistory()));
    }
}
