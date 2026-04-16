package ru.practicum.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager, "TaskManager не должен быть null");
        assertNotNull(manager.getAllTasks(), "Список задач должен быть инициализирован");
        assertNotNull(manager.getAllEpics(), "Список эпиков должен быть инициализирован");
        assertNotNull(manager.getAllSubtasks(), "Список подзадач должен быть инициализирован");
        assertNotNull(manager.getHistory(), "История должна быть инициализирована");
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не должен быть null");
        assertNotNull(historyManager.getHistory(), "История должна быть инициализирована");
    }
}