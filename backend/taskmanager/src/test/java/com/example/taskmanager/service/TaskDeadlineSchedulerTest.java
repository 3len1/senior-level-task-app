package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDeadlineSchedulerTest {

    @Mock TaskRepository taskRepository;
    @Mock SimpMessagingTemplate broker;

    @InjectMocks TaskDeadlineScheduler scheduler;

    @Test
    void notifyExpiredTasks_marksAndBroadcastsOnBothTopics() {
        var project = Project.builder().id(5L).name("P").build();
        var task = Task.builder().id(10L).title("Overdue").deadline(Instant.now().minusSeconds(60)).project(project).expiredNotified(false).build();
        when(taskRepository.findByDeadlineBeforeAndExpiredNotifiedFalse(any(Instant.class)))
                .thenReturn(List.of(task));

        scheduler.notifyExpiredTasks();

        // Task should be saved with expiredNotified=true
        ArgumentCaptor<Task> savedCap = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(savedCap.capture());
        assertTrue(savedCap.getValue().isExpiredNotified());

        // Two broadcasts: project-specific and global
        verify(broker).convertAndSend(eq("/topic/projects/5/tasks"), any(Object.class));
        verify(broker).convertAndSend(eq("/topic/tasks"), any(Object.class));
    }
}
