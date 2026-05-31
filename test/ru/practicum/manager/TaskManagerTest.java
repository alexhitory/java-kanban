package ru.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected abstract T createManager();

    protected LocalDateTime time(int hour) {
        return LocalDateTime.of(2026, 1, 1, hour, 0);
    }

    @Test
    void shouldCreateAndGetTaskById() {
        T manager = createManager();
        Task created = manager.createTask(new Task("task", "desc", Status.NEW));

        Task loaded = manager.getTaskById(created.getId());

        assertEquals(created.getId(), loaded.getId());
        assertEquals("task", loaded.getTitle());
    }

    @Test
    void shouldCreateSubtaskOnlyForExistingEpic() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        Subtask subtask = manager.createSubtask(new Subtask("sub", "desc", Status.NEW, epic.getId()));

        assertNotNull(subtask);
        assertEquals(epic.getId(), subtask.getEpicId());
        assertTrue(manager.getEpicById(epic.getId()).getSubtaskIds().contains(subtask.getId()));
        assertNull(manager.createSubtask(new Subtask("bad", "desc", Status.NEW, 999)));
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksAreNew() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        manager.createSubtask(new Subtask("s1", "desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.NEW, epic.getId()));

        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksAreDone() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        manager.createSubtask(new Subtask("s1", "desc", Status.DONE, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.DONE, epic.getId()));

        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksAreNewAndDone() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        manager.createSubtask(new Subtask("s1", "desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.DONE, epic.getId()));

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtaskIsInProgress() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        manager.createSubtask(new Subtask("s1", "desc", Status.IN_PROGRESS, epic.getId()));

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        T manager = createManager();
        Epic epic = manager.createEpic(new Epic("epic", "desc"));

        manager.createSubtask(new Subtask("s1", "desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), time(10)));
        manager.createSubtask(new Subtask("s2", "desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(45), time(12)));

        Epic loaded = manager.getEpicById(epic.getId());

        assertEquals(Duration.ofMinutes(75), loaded.getDuration());
        assertEquals(time(10), loaded.getStartTime());
        assertEquals(time(12).plusMinutes(45), loaded.getEndTime());
    }

    @Test
    void shouldReturnPrioritizedTasksByStartTime() {
        T manager = createManager();
        Task late = manager.createTask(new Task("late", "desc", Status.NEW,
                Duration.ofMinutes(30), time(14)));
        Task noTime = manager.createTask(new Task("no", "desc", Status.NEW));
        Task early = manager.createTask(new Task("early", "desc", Status.NEW,
                Duration.ofMinutes(30), time(9)));

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(List.of(early.getId(), late.getId()),
                prioritized.stream().map(Task::getId).toList());
        assertFalse(prioritized.stream().anyMatch(task -> task.getId() == noTime.getId()));
    }

    @Test
    void shouldRejectIntersectingTasksOnCreate() {
        T manager = createManager();

        Task first = manager.createTask(new Task("first", "desc", Status.NEW,
                Duration.ofMinutes(60), time(10)));
        Task intersected = manager.createTask(new Task("second", "desc", Status.NEW,
                Duration.ofMinutes(30), time(10).plusMinutes(30)));

        assertNotNull(first);
        assertNull(intersected);
        assertEquals(1, manager.getPrioritizedTasks().size());
    }

    @Test
    void shouldAllowTouchingButNotOverlappingIntervals() {
        T manager = createManager();

        Task first = manager.createTask(new Task("first", "desc", Status.NEW,
                Duration.ofMinutes(60), time(10)));
        Task second = manager.createTask(new Task("second", "desc", Status.NEW,
                Duration.ofMinutes(30), time(11)));

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(2, manager.getPrioritizedTasks().size());
    }

    @Test
    void shouldIgnoreIntersectingUpdate() {
        T manager = createManager();
        Task first = manager.createTask(new Task("first", "desc", Status.NEW,
                Duration.ofMinutes(60), time(10)));
        Task second = manager.createTask(new Task("second", "desc", Status.NEW,
                Duration.ofMinutes(30), time(12)));

        second.setStartTime(time(10).plusMinutes(15));
        manager.updateTask(second);

        assertEquals(time(12), manager.getTaskById(second.getId()).getStartTime());
        assertEquals(first.getId(), manager.getPrioritizedTasks().get(0).getId());
    }

    @Test
    void shouldUpdateAndRemoveTask() {
        T manager = createManager();
        Task task = manager.createTask(new Task("old", "desc", Status.NEW));

        task.setTitle("new");
        task.setStatus(Status.DONE);
        manager.updateTask(task);
        manager.removeTaskById(task.getId());

        assertNull(manager.getTaskById(task.getId()));
        assertTrue(manager.getAllTasks().isEmpty());
    }
}
