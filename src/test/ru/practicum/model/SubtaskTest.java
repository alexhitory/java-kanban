package ru.practicum.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic", "Big task");
        epic.setId(1);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());

        subtask.setEpicId(subtask.getId());

        assertNotEquals(
                subtask.getId(),
                subtask.getEpicId(),
                "Subtask не может быть своим эпиком"
        );
    }
}