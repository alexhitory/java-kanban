package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTasksInOrder() {
        Task t1 = new Task("1", "d1", Status.NEW);
        t1.setId(1);

        Task t2 = new Task("2", "d2", Status.NEW);
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }

    @Test
    void shouldRemoveDuplicatesAndKeepLast() {
        Task t = new Task("1", "d1", Status.NEW);
        t.setId(1);

        historyManager.add(t);
        historyManager.add(t);
        historyManager.add(t);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(1, history.get(0).getId());
    }

    @Test
    void shouldMoveTaskToEndWhenAddedAgain() {
        Task t1 = new Task("1", "d", Status.NEW);
        t1.setId(1);

        Task t2 = new Task("2", "d", Status.NEW);
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(1, history.get(1).getId());
    }

    @Test
    void shouldRemoveElement() {
        Task t = new Task("1", "d", Status.NEW);
        t.setId(1);

        historyManager.add(t);
        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldReturnEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveFromBeginningMiddleAndEnd() {
        Task t1 = new Task("1", "d", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("2", "d", Status.NEW);
        t2.setId(2);
        Task t3 = new Task("3", "d", Status.NEW);
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(1);
        assertEquals(List.of(2, 3), historyManager.getHistory().stream().map(Task::getId).toList());

        historyManager.remove(3);
        assertEquals(List.of(2), historyManager.getHistory().stream().map(Task::getId).toList());

        historyManager.add(t1);
        historyManager.add(t3);
        historyManager.remove(1);
        assertEquals(List.of(2, 3), historyManager.getHistory().stream().map(Task::getId).toList());
    }

    @Test
    void shouldIgnoreNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void historyShouldNotChangeIfTaskIsModifiedExternally() {
        Task task = new Task("Task", "Desc", Status.NEW);
        task.setId(1);

        historyManager.add(task);

        task.setTitle("HACKED");
        task.setStatus(Status.DONE);

        Task fromHistory = historyManager.getHistory().get(0);

        assertEquals("Task", fromHistory.getTitle());
        assertEquals(Status.NEW, fromHistory.getStatus());
    }

    @Test
    void shouldNotCreateDuplicatesInHistory() {
        Task t = new Task("1", "d", Status.NEW);
        t.setId(1);

        historyManager.add(t);
        historyManager.add(t);

        long count = historyManager.getHistory().stream()
                .filter(task -> task.getId() == 1)
                .count();

        assertEquals(1, count);
    }
}
