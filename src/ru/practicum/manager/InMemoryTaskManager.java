package ru.practicum.manager;

import ru.practicum.model.Status;
import ru.practicum.model.Task;
import ru.practicum.model.Subtask;
import ru.practicum.model.Epic;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    // ===== История просмотров =====
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task);
        }
    }

    // ===== Создание задач =====
    @Override
    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null; // эпика нет — подзадача не добавляется
        }
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);

        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    // ===== Получение всех задач =====
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // ===== Получение по ID =====
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        addToHistory(task);
        return task;
    }

    @Override
    public void getEpicById(int id) {
        Epic epic = epics.get(id);
        addToHistory(epic);
    }

    @Override
    public void getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        addToHistory(subtask);
    }

    // ===== Удаление всех задач =====
    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    // ===== Удаление по ID =====
    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic);
            }
        }
    }

    // ===== Обновление задач =====
    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    // ===== Получение подзадач определённого эпика =====
    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> result = new ArrayList<>();
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }
        return result;
    }

    // ===== Логика статуса эпика =====
    private void updateEpicStatus(Epic epic) {
        List<Subtask> subs = getSubtasksOfEpic(epic.getId());
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask sub : subs) {
            if (sub.getStatus() != Status.DONE) allDone = false;
            if (sub.getStatus() != Status.NEW) allNew = false;
        }

        if (allDone) epic.setStatus(Status.DONE);
        else if (allNew) epic.setStatus(Status.NEW);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}
