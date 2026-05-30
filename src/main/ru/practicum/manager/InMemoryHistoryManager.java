package ru.practicum.manager;

import ru.practicum.model.Epic;
import ru.practicum.model.Subtask;
import ru.practicum.model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head;
    private Node tail;
    private final Map<Integer, Node> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        Task taskCopy = copyTask(task);

        remove(task.getId());
        linkLast(taskCopy);
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);

        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) return;

        Node prev = node.prev;
        Node next = node.next;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }

        // Обнуляем связи для помощи GC
        node.prev = null;
        node.next = null;
        node.task = null;
    }

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;

        while (current != null) {
            result.add(current.task);
            current = current.next;
        }

        return result;
    }

    private Task copyTask(Task task) {
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            Epic copy = new Epic(epic.getTitle(), epic.getDescription());
            copy.setId(epic.getId());
            copy.setStatus(epic.getStatus());
            for (Integer id : epic.getSubtaskIds()) {
                copy.addSubtask(id);
            }
            copy.setCalculatedTime(epic.getDuration(), epic.getStartTime(), epic.getEndTime());
            return copy;
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
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
        } else {
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

    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}
