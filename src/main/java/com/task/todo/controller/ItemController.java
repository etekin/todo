package com.task.todo.controller;

import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import com.task.todo.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable("id") UUID id) {
        ItemDto itemDto = itemService.findById(id);
        return ResponseEntity.ok(itemDto);
    }

    @PostMapping("/items")
    public ResponseEntity<UUID> saveItem(@RequestBody @Valid ItemDto itemDto) {
        UUID itemId = itemService.createItem(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemId);
    }

    @PatchMapping("/items/{id}")
    public void updatePartialItem(@PathVariable("id") UUID id,
                                  @RequestBody ItemDto itemDto) {
        itemService.updatePartialItem(id, itemDto);
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemDto>> getItems(@RequestParam(name = "itemStatus", required = false) ItemStatus itemStatus) {
        List<ItemDto> items = itemService.findByStatus(Optional.ofNullable(itemStatus));
        return ResponseEntity.ok(items);
    }

}
