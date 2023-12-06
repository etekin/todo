package com.task.todo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ItemDto {
    private UUID id;
    @NotBlank(message = "description may not be empty")
    @Size(min = 1, max = 500, message = "description must be between 1 and 500 characters long")
    private String description;
    private ItemStatus status;
    private LocalDateTime createdDate;
    @NotNull(message = "dueDate may not be null")
    private LocalDateTime dueDate;
    private LocalDateTime completedDate;
    private LocalDateTime lastModifiedDate;
}
