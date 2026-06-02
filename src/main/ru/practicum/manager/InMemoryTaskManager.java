package ru.practicum.manager;

import ru.practicum.model.Status;
import ru.practicum.model.Task;
import ru.practicum.model.Subtask;
import ru.practicum.model.Epic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;
    private int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparingInt(Task::getId)
    );

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = historyManager.getHistory();
        List<Task> result = new ArrayList<>(history.size());
        for (Task task : history) {
            result.add(copyTask(task));
        }
        return result;
    }

    private void addToHistory(Task task) {
        if (task != null) {
            historyManager.add(task);
        }
    }

    protected Task copyTask(Task task) {
        switch (task.getType()) {
            case EPIC:
                return copyEpic((Epic) task);
            case SUBTASK:
                return copySubtask((Subtask) task);
            case TASK:
            default:
                Task copy = new Task(
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDuration(),
                        task.getStartTime()
                );
                copy.setId(task.getId());
                return copy;
        }
    }

    protected Epic copyEpic(Epic epic) {
        Epic copy = new Epic(epic.getTitle(), epic.getDescription());
        copy.setId(epic.getId());
        copy.setStatus(epic.getStatus());
        for (Integer subtaskId : epic.getSubtaskIds()) {
            copy.addSubtask(subtaskId);
        }
        copy.setCalculatedTime(epic.getDuration(), epic.getStartTime(), epic.getEndTime());
        return copy;
    }

    protected Subtask copySubtask(Subtask subtask) {
        Subtask copy = new Subtask(
                subtask.getTitle(),
                subtask.getDescription(),
                subtask.getStatus(),
                subtask.getEpicId(),
                subtask.getDuration(),
                subtask.getStartTime()
        );
        copy.setId(subtask.getId());
        return copy;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) return null;

        Task stored = copyTask(task);
        stored.setId(nextId);
        if (hasTimeIntersection(stored)) return null;

        nextId++;
        tasks.put(stored.getId(), stored);
        addToPrioritizedTasks(stored);
        return copyTask(stored);
    }

    protected void restoreTask(Task task) {
        Task stored = copyTask(task);
        if (hasTimeIntersection(stored)) return;

        tasks.put(stored.getId(), stored);
        addToPrioritizedTasks(stored);
        updateNextId(stored.getId());
    }

    protected void restoreEpic(Epic epic) {
        Epic stored = copyEpic(epic);
        epics.put(stored.getId(), stored);
        updateNextId(stored.getId());
    }

    protected void restoreSubtask(Subtask subtask) {
        Subtask stored = copySubtask(subtask);
        if (hasTimeIntersection(stored)) return;

        subtasks.put(stored.getId(), stored);
        addToPrioritizedTasks(stored);

        Epic epic = epics.get(stored.getEpicId());
        if (epic != null) {
            epic.addSubtask(stored.getId());
            recalculateEpic(epic);
        }

        updateNextId(stored.getId());
    }

    private void updateNextId(int restoredId) {
        nextId = Math.max(nextId, restoredId + 1);
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;

        Epic stored = copyEpic(epic);
        stored.setId(nextId++);
        stored.setCalculatedTime(Duration.ZERO, null, null);
        epics.put(stored.getId(), stored);
        return copyEpic(stored);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) return null;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return null;

        Subtask stored = copySubtask(subtask);
        stored.setId(nextId);
        if (hasTimeIntersection(stored)) return null;

        nextId++;
        subtasks.put(stored.getId(), stored);
        addToPrioritizedTasks(stored);

        epic.addSubtask(stored.getId());
        recalculateEpic(epic);

        return copySubtask(stored);
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>(tasks.size());
        for (Task task : tasks.values()) {
            result.add(copyTask(task));
        }
        return result;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> result = new ArrayList<>(epics.size());
        for (Epic epic : epics.values()) {
            result.add(copyEpic(epic));
        }
        return result;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> result = new ArrayList<>(subtasks.size());
        for (Subtask subtask : subtasks.values()) {
            result.add(copySubtask(subtask));
        }
        return result;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .map(this::copyTask)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        addToHistory(task);
        return task == null ? null : copyTask(task);
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        addToHistory(epic);
        return epic == null ? null : copyEpic(epic);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        addToHistory(subtask);
        return subtask == null ? null : copySubtask(subtask);
    }

    @Override
    public void removeAllTasks() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.values().forEach(this::removeFromPrioritizedTasks);
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        subtasks.clear();

        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }

        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            recalculateEpic(epic);
        }
    }

    @Override
    public void removeTaskById(int id) {
        Task task = tasks.remove(id);
        removeFromPrioritizedTasks(task);
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subId);
                removeFromPrioritizedTasks(subtask);
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask != null) {
            removeFromPrioritizedTasks(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                recalculateEpic(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            Task stored = copyTask(task);
            if (hasTimeIntersection(stored)) return;

            removeFromPrioritizedTasks(tasks.get(task.getId()));
            tasks.put(task.getId(), stored);
            addToPrioritizedTasks(stored);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) return;

        Epic existing = epics.get(epic.getId());
        if (existing != null) {
            boolean changed = false;

            if (!existing.getTitle().equals(epic.getTitle())) {
                existing.setTitle(epic.getTitle());
                changed = true;
            }

            if (!existing.getDescription().equals(epic.getDescription())) {
                existing.setDescription(epic.getDescription());
                changed = true;
            }

            if (changed) {
                recalculateEpic(existing);
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) return;

        Subtask existing = subtasks.get(subtask.getId());
        if (existing == null) return;

        if (existing.getEpicId() != subtask.getEpicId()) return;

        Subtask stored = copySubtask(subtask);
        if (hasTimeIntersection(stored)) return;

        removeFromPrioritizedTasks(existing);
        subtasks.put(subtask.getId(), stored);
        addToPrioritizedTasks(stored);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            recalculateEpic(epic);
        }
    }

    private void recalculateEpic(Epic epic) {
        List<Subtask> subs = getSubtasksOfEpic(epic.getId());

        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setCalculatedTime(Duration.ZERO, null, null);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask s : subs) {
            if (s.getStatus() != Status.NEW) allNew = false;
            if (s.getStatus() != Status.DONE) allDone = false;
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }

        Duration duration = subs.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        LocalDateTime startTime = subs.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime endTime = subs.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setCalculatedTime(duration, startTime, endTime);
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return new ArrayList<>();

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(this::copySubtask)
                .collect(Collectors.toList());
    }

    private void addToPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    public boolean hasTimeIntersection(Task task) {
        if (task == null || task.getStartTime() == null) return false;

        return prioritizedTasks.stream()
                .anyMatch(existing -> existing.getId() != task.getId() && isTimeIntersected(existing, task));
    }

    public boolean isTimeIntersected(Task first, Task second) {
        if (first.getStartTime() == null || second.getStartTime() == null) return false;

        LocalDateTime firstEnd = first.getEndTime();
        LocalDateTime secondEnd = second.getEndTime();

        return first.getStartTime().isBefore(secondEnd) && firstEnd.isAfter(second.getStartTime());
    }
}
