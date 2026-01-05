package ru.practicum.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (subtaskId != this.getId()) { // нельзя добавить самого себя
            subtaskIds.add(subtaskId);
        }
    }


    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

}
