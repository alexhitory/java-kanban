package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskIntegrityTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldRemoveSubtaskIdFromEpicAfterDeletion() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask sub = manager.createSubtask(
                new Subtask("s", "d", Status.NEW, epic.getId())
        );

        manager.removeSubtaskById(sub.getId());

        Epic updated = manager.getEpicById(epic.getId());

        assertFalse(updated.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void shouldClearSubtasksWhenEpicDeleted() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask sub = manager.createSubtask(
                new Subtask("s", "d", Status.NEW, epic.getId())
        );

        manager.removeEpicById(epic.getId());

        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldRemoveTaskFromHistoryAfterDeletion() {
        Task task = manager.createTask(new Task("t", "d", Status.NEW));

        manager.getTaskById(task.getId());
        manager.removeTaskById(task.getId());

        assertFalse(manager.getHistory().contains(task));
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask sub = new Subtask("s", "d", Status.NEW, 999);

        Subtask result = manager.createSubtask(sub);

        assertNull(result);
    }

    @Test
    void shouldKeepHistoryAfterUpdate() {
        Task task = manager.createTask(new Task("t", "d", Status.NEW));

        manager.getTaskById(task.getId());

        task.setTitle("changed");
        manager.updateTask(task);

        assertEquals(1, manager.getHistory().size());
    }
}