package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    void setup() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Task", "Desc", Status.NEW);
        task.setId(1);
    }

    @Test
    void addAndRetrieveHistory() {
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать добавленную задачу");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной");
    }

    @Test
    void historyLimitIsRespected() {
        for (int i = 1; i <= 15; i++) {
            Task t = new Task("Task" + i, "Desc", Status.NEW);
            t.setId(i);
            historyManager.add(t);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История не должна превышать лимит в 10 задач");
        assertEquals(6, history.get(0).getId(), "Старые задачи удаляются при переполнении");
    }
}
