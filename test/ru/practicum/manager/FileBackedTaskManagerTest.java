package ru.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            return new FileBackedTaskManager(File.createTempFile("tasks", ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldLoadFromEmptyFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 10, 0);
        Task task = manager.createTask(new Task("Task1", "Description task1", Status.NEW,
                Duration.ofMinutes(15), startTime));
        Epic epic = manager.createEpic(new Epic("Epic2", "Description epic2"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub Task2", "Description sub task3", Status.DONE, epic.getId(),
                        Duration.ofMinutes(20), startTime.plusHours(1))
        );

        String saved = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        assertTrue(saved.contains("id,type,name,status,description,duration,startTime,epic"));
        assertTrue(saved.contains(task.getId() + ",TASK,Task1,NEW,Description task1,15," + startTime + ","));
        assertTrue(saved.contains(epic.getId() + ",EPIC,Epic2,DONE,Description epic2,20," + startTime.plusHours(1)
                + ","));
        assertTrue(saved.contains(subtask.getId() + ",SUBTASK,Sub Task2,DONE,Description sub task3,20,"
                + startTime.plusHours(1) + "," + epic.getId()));
    }

    @Test
    void shouldLoadSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 10, 0);
        Task task = manager.createTask(new Task("Task1", "Description task1", Status.NEW,
                Duration.ofMinutes(15), startTime));
        Epic epic = manager.createEpic(new Epic("Epic2", "Description epic2"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub Task2", "Description sub task3", Status.DONE, epic.getId(),
                        Duration.ofMinutes(20), startTime.plusHours(1))
        );

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(task.getTitle(), loaded.getTaskById(task.getId()).getTitle());
        assertEquals(Duration.ofMinutes(15), loaded.getTaskById(task.getId()).getDuration());
        assertEquals(startTime, loaded.getTaskById(task.getId()).getStartTime());
        assertEquals(Status.DONE, loaded.getEpicById(epic.getId()).getStatus());
        assertEquals(Duration.ofMinutes(20), loaded.getEpicById(epic.getId()).getDuration());
        assertEquals(subtask.getEpicId(), loaded.getSubtaskById(subtask.getId()).getEpicId());
        assertEquals(1, loaded.getSubtasksOfEpic(epic.getId()).size());
    }

    @Test
    void shouldWrapFileReadException() {
        File directory = new File(System.getProperty("java.io.tmpdir"));

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(directory));
    }

    @Test
    void shouldNotThrowForValidFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(file));
    }
}
