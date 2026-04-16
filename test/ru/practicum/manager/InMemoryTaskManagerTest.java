package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;
import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldNotBeAffectedByExternalChangesAfterCreation() {
        Task task = new Task("t", "d", Status.NEW);

        Task created = manager.createTask(task);

        task.setTitle("HACKED");
        task.setStatus(Status.DONE);

        Task stored = manager.getTaskById(created.getId());

        assertEquals("t", stored.getTitle());
        assertEquals(Status.NEW, stored.getStatus());
    }

    @Test
    void historyShouldNotBeAffectedByExternalChanges() {
        Task task = manager.createTask(new Task("t", "d", Status.NEW));

        manager.getTaskById(task.getId());

        task.setTitle("MODIFIED");
        task.setStatus(Status.DONE);

        Task fromHistory = manager.getHistory().get(0);

        assertEquals("t", fromHistory.getTitle());
        assertEquals(Status.NEW, fromHistory.getStatus());
    }

    @Test
    void modifyingObjectFromHistoryShouldNotAffectManager() {
        Task task = manager.createTask(new Task("t", "d", Status.NEW));

        manager.getTaskById(task.getId());

        Task fromHistory = manager.getHistory().get(0);

        fromHistory.setTitle("BROKEN");
        fromHistory.setStatus(Status.DONE);

        Task stored = manager.getTaskById(task.getId());

        assertEquals("t", stored.getTitle());
        assertEquals(Status.NEW, stored.getStatus());
    }

    @Test
    void shouldNotLeaveDanglingSubtaskIdsInEpic() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask sub = manager.createSubtask(
                new Subtask("s", "d", Status.NEW, epic.getId())
        );

        manager.removeSubtaskById(sub.getId());

        Epic updated = manager.getEpicById(epic.getId());

        assertFalse(updated.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void epicShouldHaveNoBrokenSubtaskReferencesAfterDeletion() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask s1 = manager.createSubtask(new Subtask("s1", "d", Status.NEW, epic.getId()));
        Subtask s2 = manager.createSubtask(new Subtask("s2", "d", Status.NEW, epic.getId()));

        manager.removeEpicById(epic.getId());

        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void removeAllTasksShouldClearHistoryAndData() {
        Task t1 = manager.createTask(new Task("t1", "d", Status.NEW));
        manager.getTaskById(t1.getId());

        manager.removeAllTasks();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void removeAllEpicsShouldClearSubtasksAndHistory() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask s = manager.createSubtask(
                new Subtask("s", "d", Status.NEW, epic.getId())
        );

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(s.getId());

        manager.removeAllEpics();

        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void getAllTasksShouldReturnCopyNotInternalList() {
        Task t = manager.createTask(new Task("t", "d", Status.NEW));

        List<Task> list = manager.getAllTasks();
        list.clear();

        assertFalse(manager.getAllTasks().isEmpty());
    }

    @Test
    void getHistoryShouldReturnCopyNotInternalStructure() {
        Task t = manager.createTask(new Task("t", "d", Status.NEW));

        manager.getTaskById(t.getId());

        List<Task> history = manager.getHistory();
        history.clear();

        assertFalse(manager.getHistory().isEmpty());
    }

    @Test
    void epicStatusShouldRecalculateCorrectly() {
        Epic epic = manager.createEpic(new Epic("e", "d"));

        Subtask s1 = manager.createSubtask(new Subtask("s1", "d", Status.NEW, epic.getId()));
        Subtask s2 = manager.createSubtask(new Subtask("s2", "d", Status.NEW, epic.getId()));

        s1.setStatus(Status.DONE);
        manager.updateSubtask(s1);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }
}