package com.task.todo.service;

import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import com.task.todo.repository.ItemRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@ActiveProfiles("test")
class TodoApplicationUnitTests {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    private Validator validator;

    @BeforeEach
    void clearDatabase(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @BeforeClass
    public void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testCreateItem_withoutDueDate() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.createItem(item));
    }

    @Test
    void testCreateItem_withoutStatus() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        UUID itemId = itemService.createItem(item);
        Optional<ItemEntity> optionalItem = itemRepository.findById(itemId);
        assertTrue(optionalItem.isPresent());
        ItemEntity itemEntity = optionalItem.get();
        assertNotNull(itemEntity.getId());
        assertEquals("test description", itemEntity.getDescription());
        assertNotNull(itemEntity.getCreatedDate());
        assertNotNull(itemEntity.getDueDate());
        assertNull(itemEntity.getCompletedDate());
        assertNotNull(itemEntity.getLastModifiedDate());
        assertEquals(itemEntity.getStatus(), ItemStatus.UNDONE);
    }

    @Test
    void testCreateItem_withDoneStatus() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        item.setStatus(ItemStatus.DONE);
        UUID itemId = itemService.createItem(item);
        Optional<ItemEntity> optionalItem = itemRepository.findById(itemId);
        assertTrue(optionalItem.isPresent());
        ItemEntity itemEntity = optionalItem.get();
        assertNotNull(itemEntity.getId());
        assertEquals("test description", itemEntity.getDescription());
        assertNotNull(itemEntity.getCreatedDate());
        assertNotNull(itemEntity.getDueDate());
        assertNotNull(itemEntity.getCompletedDate());
        assertNotNull(itemEntity.getLastModifiedDate());
        assertEquals(itemEntity.getStatus(), ItemStatus.DONE);
    }

    @Test
    void testCreateItem_withUndoneStatusAndWithCompletedDate() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        item.setStatus(ItemStatus.UNDONE);
        item.setCompletedDate(LocalDateTime.now());
        assertThrows(ResponseStatusException.class, () -> itemService.createItem(item));
    }

    @Test
    void testCreateItem_withEmptyDescription() {
        ItemDto item = new ItemDto();
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        assertThrows(DataIntegrityViolationException.class, () -> itemService.createItem(item));
    }


    @Test
    void testUpdateItem_withDueDatePassed() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
        item.setStatus(ItemStatus.DUE_DATE_PASSED);
        UUID itemId = itemService.createItem(item);

        ItemDto updatingItem = new ItemDto();
        updatingItem.setDescription("updated description");
        assertThrows(ResponseStatusException.class, () -> itemService.updatePartialItem(itemId, item));
    }

    @Test
    @SneakyThrows
    void testBulkUpdate() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.parse("2023-11-06T10:00:00"));
        UUID itemId = itemService.createItem(item);

        ItemDto item2 = new ItemDto();
        item2.setDescription("test2 description");
        item2.setDueDate(LocalDateTime.parse("2023-11-06T10:01:00"));
        UUID itemId2 = itemService.createItem(item2);

        List<UUID> uuids = itemService.updateDueDates();
        assertTrue(uuids.containsAll(List.of(itemId, itemId2)));

        List<ItemDto> items = itemService.findByStatus(Optional.of(ItemStatus.DUE_DATE_PASSED));
        assertEquals(items.size(), 2);
    }


    @Test
    @SneakyThrows
    @Disabled
    void testAutoUpdate_whenReachedToDueDate() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        LocalDateTime now = LocalDateTime.now();
        item.setDueDate(now.plus(3, ChronoUnit.SECONDS));
        UUID itemId = itemService.createItem(item);
        Thread.sleep(10000);
        List<ItemDto> items = itemService.findByStatus(Optional.of(ItemStatus.DUE_DATE_PASSED));
        assertEquals(items.size(), 1);

    }


    //TODO other unit tests should be added

}
