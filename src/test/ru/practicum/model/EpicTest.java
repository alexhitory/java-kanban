package ru.practicum.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void testEpicCannotContainItself() {
        Epic epic = new Epic("Epic", "Big task");
        epic.setId(1);

        epic.addSubtask(epic.getId());

        assertFalse(
                epic.getSubtaskIds().contains(epic.getId()),
                "Эпик не должен содержать сам себя"
        );
    }
}