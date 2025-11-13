package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.enums.TaskStatus;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDeadlineSchedulerTest {

    @Mock TaskRepository taskRepository;
    @Mock SimpMessagingTemplate broker;

    @InjectMocks TaskDeadlineScheduler scheduler;

    @Test
    void notifyExpiredTasks_marksNotified_andBroadcasts() {
        var project = Project.builder().id(42L).build();
        var expired = Task.builder()
                .id(7L)
                .title("Overdue")
                .status(TaskStatus.TODO)
                .project(project)
                .deadline(Instant.now().minusSeconds(60))
                .expiredNotified(false)
                .build();

        when(taskRepository.findByDeadlineBeforeAndExpiredNotifiedFalse(any(Instant.class)))
                .thenReturn(List.of(expired));

        scheduler.notifyExpiredTasks();

        // saved with expiredNotified true
        verify(taskRepository, times(1)).save(argThat(t -> t.getId().equals(7L) && t.isExpiredNotified()));

        // broadcasted to the correct topic with expected payload
        ArgumentCaptor<Map<String, Object>> payloadCap = ArgumentCaptor.forClass(Map.class);
        verify(broker).convertAndSend(eq("/topic/projects/42/tasks"), payloadCap.capture());

        Map<String, Object> payload = payloadCap.getValue();
        assertEquals("expired", payload.get("action"));
        assertEquals(7L, payload.get("taskId"));
        assertEquals(42L, payload.get("projectId"));
        assertNotNull(payload.get("deadline"));
    }
}
