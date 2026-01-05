package ru.practicum.Test;

import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Title", "Desc", Status.NEW);
        Task task2 = new Task("Title2", "Desc2", Status.IN_PROGRESS);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void testTaskInequalityDifferentId() {
        Task task1 = new Task("Title", "Desc", Status.NEW);
        Task task2 = new Task("Title", "Desc", Status.NEW);

        task1.setId(1);
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разными id не должны быть равны");
    }

    @Test
    void testEpicCannotContainItself() {
        Epic epic = new Epic("Epic", "Big task");
        epic.setId(1);
        epic.addSubtask(epic.getId());

        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Эпик не должен содержать сам себя");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic", "Big task");
        epic.setId(1);
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic.getId());
        subtask.setEpicId(subtask.getId());

        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Subtask не может быть своим эпиком");
    }
}
