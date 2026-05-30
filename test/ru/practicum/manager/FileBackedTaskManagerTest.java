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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {

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

        Task task = manager.createTask(new Task("Task1", "Description task1", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Epic2", "Description epic2"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub Task2", "Description sub task3", Status.DONE, epic.getId())
        );

        String saved = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        assertTrue(saved.contains("id,type,name,status,description,epic"));
        assertTrue(saved.contains(task.getId() + ",TASK,Task1,NEW,Description task1,"));
        assertTrue(saved.contains(epic.getId() + ",EPIC,Epic2,DONE,Description epic2,"));
        assertTrue(saved.contains(subtask.getId() + ",SUBTASK,Sub Task2,DONE,Description sub task3," + epic.getId()));
    }

    @Test
    void shouldLoadSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Task1", "Description task1", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Epic2", "Description epic2"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub Task2", "Description sub task3", Status.DONE, epic.getId())
        );

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(task.getTitle(), loaded.getTaskById(task.getId()).getTitle());
        assertEquals(Status.DONE, loaded.getEpicById(epic.getId()).getStatus());
        assertEquals(subtask.getEpicId(), loaded.getSubtaskById(subtask.getId()).getEpicId());
        assertEquals(1, loaded.getSubtasksOfEpic(epic.getId()).size());
    }
}
