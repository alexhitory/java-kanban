package ru.practicum.model;

import org.junit.jupiter.api.Test;

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
    void testEpicEqualityById() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void testEpicInequalityDifferentId() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");

        epic1.setId(1);
        epic2.setId(2);

        assertNotEquals(epic1, epic2, "Эпики с разными id не должны быть равны");
    }

    @Test
    void testSubtaskEqualityById() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);

        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.IN_PROGRESS, epic.getId());

        sub1.setId(1);
        sub2.setId(1);

        assertEquals(sub1, sub2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void testSubtaskInequalityDifferentId() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);

        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.NEW, epic.getId());

        sub1.setId(1);
        sub2.setId(2);

        assertNotEquals(sub1, sub2, "Подзадачи с разными id не должны быть равны");
    }

}
