package ru.practicum.manager;

import ru.practicum.model.Task;
import ru.practicum.model.Subtask;
import ru.practicum.model.Epic;

import java.util.List;

public interface TaskManager {
    // ===== История просмотров =====
    List<Task> getHistory();

    // ===== Создание задач =====
    Task createTask(Task task);
    Epic createEpic(Epic epic);
    Subtask createSubtask(Subtask subtask);

    // ===== Получение всех задач =====
    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();

    // ===== Получение по ID =====
    Task getTaskById(int id);
    void getEpicById(int id);
    void getSubtaskById(int id);

    // ===== Удаление всех задач =====
    void removeAllTasks();
    void removeAllEpics();
    void removeAllSubtasks();

    // ===== Удаление по ID =====
    void removeTaskById(int id);
    void removeEpicById(int id);
    void removeSubtaskById(int id);

    // ===== Обновление задач =====
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    // ===== Получение подзадач определённого эпика =====
    List<Subtask> getSubtasksOfEpic(int epicId);
}
