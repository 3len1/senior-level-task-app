package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskDeadlineScheduler {

    private final TaskRepository tasks;
    private final SimpMessagingTemplate broker;

    public TaskDeadlineScheduler(TaskRepository tasks, SimpMessagingTemplate broker) {
        this.tasks = tasks;
        this.broker = broker;
    }

    // Run every minute to check for newly expired tasks
    @Scheduled(fixedDelay = 60_000)
    public void notifyExpiredTasks() {
        List<Task> due = tasks.findByDeadlineBeforeAndExpiredNotifiedFalse(Instant.now());
        for (Task t : due) {
            t.setExpiredNotified(true);
            tasks.save(t);
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "expired");
            payload.put("taskId", t.getId());
            payload.put("projectId", t.getProject().getId());
            payload.put("deadline", t.getDeadline());
            broker.convertAndSend("/topic/projects/" + t.getProject().getId() + "/tasks", payload);
        }
    }
}
