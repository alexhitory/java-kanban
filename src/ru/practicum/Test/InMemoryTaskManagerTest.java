package ru.practicum.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Test
    void createAndGetTask() {
        Task task = new Task("Task1", "Desc", Status.NEW);
        manager.createTask(task);

        Task retrieved = manager.getTaskById(task.getId());
        assertEquals(task, retrieved, "Созданная и полученная задачи должны совпадать");

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size(), "Должна быть одна задача");
    }

    @Test
    void createEpicAndSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Big"));
        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "D1", Status.NEW, epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "D2", Status.NEW, epic.getId()));

        List<Subtask> subs = manager.getSubtasksOfEpic(epic.getId());
        assertEquals(2, subs.size(), "Должны вернуться две подзадачи");

        assertEquals(Status.NEW, epic.getStatus(), "Эпик с NEW подзадачами должен быть NEW");

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);

        assertEquals(Status.DONE, epic.getStatus(), "Эпик с DONE подзадачами должен быть DONE");
    }

    @Test
    void historyManagerTracksTasks() {
        Task task = manager.createTask(new Task("Task", "Desc", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "D", Status.NEW, epic.getId()));

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        manager.getTaskById(task.getId()); // повторный просмотр

        List<Task> history = manager.getHistory();
        assertEquals(4, history.size(), "История должна содержать все просмотры, включая дубликаты");
    }

    @Test
    void removeTaskRemovesItFromManager() {
        Task task = manager.createTask(new Task("Task", "Desc", Status.NEW));
        manager.removeTaskById(task.getId());
        assertNull(manager.getTaskById(task.getId()), "Удалённая задача не должна возвращаться");
    }
}
