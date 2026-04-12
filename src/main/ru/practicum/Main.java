package ru.practicum;

import ru.practicum.manager.Managers;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.*;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        // ===== 1. Создаём две обычные задачи =====
        Task task1 = manager.createTask(
                new Task("Купить продукты", "Молоко, хлеб, яйца", Status.NEW)
        );
        Task task2 = manager.createTask(
                new Task("Позвонить маме", "Узнать, как дела", Status.IN_PROGRESS)
        );

        // ===== 2. Эпик с двумя подзадачами =====
        Epic epic1 = manager.createEpic(
                new Epic("Переезд", "Переезд в новую квартиру")
        );

        Subtask subtask1 = manager.createSubtask(
                new Subtask("Собрать вещи", "Упаковать коробки", Status.NEW, epic1.getId())
        );
        Subtask subtask2 = manager.createSubtask(
                new Subtask("Заказать грузчиков", "Найти транспорт", Status.NEW, epic1.getId())
        );

        // ===== 3. Эпик с одной подзадачей =====
        Epic epic2 = manager.createEpic(
                new Epic("Отпуск", "Подготовка к отпуску")
        );

        Subtask subtask3 = manager.createSubtask(
                new Subtask("Купить билеты", "Самолёт туда и обратно", Status.NEW, epic2.getId())
        );

        // ===== 4. Печать всех задач =====
        System.out.println("\n=== Задачи ===");
        System.out.println(manager.getAllTasks());

        System.out.println("\n=== Эпики ===");
        System.out.println(manager.getAllEpics());

        System.out.println("\n=== Подзадачи ===");
        System.out.println(manager.getAllSubtasks());

        // ===== 5. Меняем статусы =====
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        // ===== 6. Проверка статусов =====
        System.out.println("\n=== Статусы после изменений ===");

        for (Task task : manager.getAllTasks()) {
            System.out.println("Task " + task.getId() +
                    " (" + task.getTitle() + "): " + task.getStatus());
        }

        for (Epic epic : manager.getAllEpics()) {
            System.out.println("Epic " + epic.getId() +
                    " (" + epic.getTitle() + "): " + epic.getStatus() +
                    ", подзадач: " + epic.getSubtaskIds().size());
        }

        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("Subtask " + subtask.getId() +
                    " (" + subtask.getTitle() + "): " + subtask.getStatus() +
                    ", epicId=" + subtask.getEpicId());
        }

        // ===== 7. Удаление =====
        manager.removeTaskById(task2.getId());
        manager.removeEpicById(epic1.getId());

        // ===== 8. Проверка после удаления =====
        System.out.println("\n=== После удаления ===");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        // ===== 9. История просмотров =====
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic2.getId());
        manager.getSubtaskById(subtask3.getId());
        manager.getTaskById(task1.getId());

        System.out.println("\n=== История ===");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
