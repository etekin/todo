package com.task.todo.service;

import com.task.todo.converter.ItemConverter;
import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import com.task.todo.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemConverter itemConverter;
    private final DueDateSchedulerService dueDateSchedulerService;


    public ItemDto findById(UUID id) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(id);
        ItemEntity itemEntity = itemEntityOptional.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, " Item not found by id:" + id));
        return itemConverter.mapToDto(itemEntity);
    }

    public void updatePartialItem(UUID id, ItemDto itemDto) {

        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(id);
        ItemEntity itemEntity = itemEntityOptional.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, " Item not found by id:" + id));
        if (itemEntity.getStatus() == ItemStatus.DUE_DATE_PASSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Items with due date passed cannot be updated");
        }
        if (itemEntity.getStatus() == ItemStatus.UNDONE &&
                itemDto.getStatus() == ItemStatus.DONE &&
                itemDto.getCompletedDate() == null) {
            itemDto.setCompletedDate(LocalDateTime.now());
        }

        if (itemEntity.getStatus() == ItemStatus.DONE &&
                itemDto.getStatus() == ItemStatus.UNDONE) {
            if (itemDto.getCompletedDate() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "In an item with UNDONE status, completed date shouldn't be filled");
            }
            itemEntity.setCompletedDate(null);
        }

        boolean dueDateUpdated = itemDto.getDueDate() != null &&
                !itemEntity.getDueDate().equals(itemDto.getDueDate());

        itemConverter.mapPartialToEntity(itemDto, itemEntity);
        ItemEntity savedItem = itemRepository.save(itemEntity);

        if (dueDateUpdated) {
            dueDateSchedulerService.addOrUpdateSchedulerForItem(savedItem);
        }

    }

    public UUID createItem(ItemDto itemDto) {
        ItemEntity itemEntity = itemConverter.mapToEntity(itemDto);
        if (itemEntity.getStatus() == null) {
            itemEntity.setStatus(ItemStatus.UNDONE);
        }
        if (itemDto.getStatus() == ItemStatus.DONE &&
                itemDto.getCompletedDate() == null) {
            itemEntity.setCompletedDate(LocalDateTime.now());
        }

        if (itemEntity.getStatus() == ItemStatus.UNDONE && itemEntity.getCompletedDate() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "In an item with UNDONE status, completed date shouldn't be filled");
        }

        ItemEntity savedItem = itemRepository.save(itemEntity);
        dueDateSchedulerService.addOrUpdateSchedulerForItem(savedItem);
        return savedItem.getId();
    }

    public List<ItemDto> findByStatus(Optional<ItemStatus> itemStatusOptional) {
        List<ItemEntity> itemEntities;
        if (itemStatusOptional.isPresent()) {
            itemEntities = itemRepository.findByStatus(itemStatusOptional.get());
        } else {
            itemEntities = itemRepository.findAll();
        }
        return itemEntities.stream().map(itemConverter::mapToDto).toList();
    }

    @Transactional
    public List<UUID> updateDueDates() {
        List<ItemEntity> undoneItems = itemRepository.findAllByDueDateLessThanEqualAndStatus(LocalDateTime.now(), ItemStatus.UNDONE);
        List<UUID> itemIds = undoneItems.stream().map(ItemEntity::getId).toList();
        itemRepository.updateItemsStatus(ItemStatus.DUE_DATE_PASSED, itemIds);
        return itemIds;
    }
}
