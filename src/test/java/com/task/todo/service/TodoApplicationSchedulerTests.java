package com.task.todo.service;

import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import com.task.todo.repository.ItemRepository;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(properties = {
        "spring.flyway.clean-disabled=false",
        "todo.duedate.cron.intervalInMillisecond=5000"})
@ActiveProfiles("test")
class TodoApplicationSchedulerTests {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void clearDatabase(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @SneakyThrows
    void testBulkUpdate_onAsyncRecords() {
        final long sixSecondsInMillis = 6000;
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        LocalDateTime now = LocalDateTime.now();
        item.setDueDate(now.plus(1, HOURS));
        UUID itemId = itemService.createItem(item);
        Optional<ItemEntity> optionalItemEntity = itemRepository.findById(itemId);
        if (optionalItemEntity.isPresent()) {
            ItemEntity itemEntity = optionalItemEntity.get();
            itemEntity.setDueDate(now.plus(1, SECONDS));
            itemRepository.save(itemEntity);
        }

        ItemDto item2 = new ItemDto();
        item2.setDescription("test2 description");
        item2.setDueDate(now.plus(1, HOURS));
        UUID itemId2 = itemService.createItem(item2);
        Optional<ItemEntity> optionalItemEntity2 = itemRepository.findById(itemId2);
        if (optionalItemEntity2.isPresent()) {
            ItemEntity itemEntity = optionalItemEntity2.get();
            itemEntity.setDueDate(now.plus(1, SECONDS));
            itemRepository.save(itemEntity);
        }
        Thread.sleep(sixSecondsInMillis);

        List<ItemDto> items = itemService.findByStatus(Optional.of(ItemStatus.DUE_DATE_PASSED));
        assertEquals(items.size(), 2);
    }

    @Test
    @SneakyThrows
    void testAutoUpdate_whenReachedToDueDate() {
        final long fourSecondsInMillis = 4000;
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        LocalDateTime now = LocalDateTime.now();
        item.setDueDate(now.plus(2, SECONDS));
        UUID itemId = itemService.createItem(item);
        Thread.sleep(fourSecondsInMillis);
        List<ItemDto> items = itemService.findByStatus(Optional.of(ItemStatus.DUE_DATE_PASSED));
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getId(), itemId);
    }

    //TODO other scheduler tests should be added

}
