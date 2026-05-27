package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with Tasks
 */
public interface TaskService {

    TaskResponseDto save(TaskCreateDto newTaskDto, AppUser authUser);

    TaskResponseDto getById(String id, AppUser authUser);

    Task getOrThrow(String id, AppUser authUser);

    Optional<Task> findById(String id, AppUser authUser);

    List<TaskResponseDto> getAll(String projectId, AppUser authUser);

    void delete(String id, AppUser changer);

    TaskResponseDto updateStatus(String id, TaskStatus status, AppUser changer);

    TaskResponseDto update(String id, TaskUpdateDto request, AppUser changer);
}
