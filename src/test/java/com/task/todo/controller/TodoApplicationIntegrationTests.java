package com.task.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import com.task.todo.repository.ItemRepository;
import com.task.todo.service.ItemService;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoApplicationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

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
    void testCreateItem() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        mockMvc.perform(post("/items").content(asJsonString(item))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        List<ItemEntity> items = itemRepository.findByStatus(ItemStatus.UNDONE);
        assertEquals(items.size(), 1);
        ItemEntity itemEntity = items.get(0);
        assertNotNull(itemEntity.getId());
        assertEquals("test description", itemEntity.getDescription());
        assertNotNull(itemEntity.getCreatedDate());
        assertNotNull(itemEntity.getDueDate());
        assertNull(itemEntity.getCompletedDate());
        assertNotNull(itemEntity.getLastModifiedDate());
        assertEquals(itemEntity.getStatus(), ItemStatus.UNDONE);

    }

    @Test
    @SneakyThrows
    void testCreateItem_withoutDueDate() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        mockMvc.perform(post("/items").content(asJsonString(item))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testCreateItem_withEmptyDescription() {
        ItemDto item = new ItemDto();
        item.setDescription("");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        mockMvc.perform(post("/items").content(asJsonString(item))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testUpdateDescriptionOfItem() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        UUID itemId = itemService.createItem(item);
        ItemDto savedItem = itemService.findById(itemId);
        savedItem.setDescription("changed description");

        mockMvc.perform(patch("/items/{id}", itemId).content(asJsonString(savedItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<ItemEntity> items = itemRepository.findByStatus(ItemStatus.UNDONE);
        assertEquals(items.size(), 1);
        ItemEntity itemEntity = items.get(0);
        assertNotNull(itemEntity.getId());
        assertEquals("changed description", itemEntity.getDescription());
        assertNotNull(itemEntity.getCreatedDate());
        assertNotNull(itemEntity.getDueDate());
        assertNull(itemEntity.getCompletedDate());
        assertNotNull(itemEntity.getLastModifiedDate());
        assertEquals(itemEntity.getStatus(), ItemStatus.UNDONE);
    }

    @Test
    @SneakyThrows
    void testMakeDone() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        UUID itemId = itemService.createItem(item);

        ItemDto updatingItem = new ItemDto();
        updatingItem.setStatus(ItemStatus.DONE);

        mockMvc.perform(patch("/items/{id}", itemId).content(asJsonString(updatingItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<ItemEntity> items = itemRepository.findByStatus(ItemStatus.DONE);
        assertEquals(items.size(), 1);
        ItemEntity itemEntity = items.get(0);
        assertNotNull(itemEntity.getId());
        assertEquals("test description", itemEntity.getDescription());
        assertNotNull(itemEntity.getCreatedDate());
        assertNotNull(itemEntity.getDueDate());
        assertNotNull(itemEntity.getCompletedDate());
        assertNotNull(itemEntity.getLastModifiedDate());
        assertEquals(itemEntity.getStatus(), ItemStatus.DONE);
    }

    @Test
    @SneakyThrows
    void testMakeUnDone() {
        ItemDto item = new ItemDto();
        item.setDescription("test description");
        item.setStatus(ItemStatus.DONE);
        item.setDueDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
        UUID itemId = itemService.createItem(item);

        ItemDto updatingItem = new ItemDto();
        updatingItem.setStatus(ItemStatus.UNDONE);

        mockMvc.perform(patch("/items/{id}", itemId).content(asJsonString(updatingItem))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ItemDto itemDto = itemService.findById(itemId);
        assertNotNull(itemDto.getId());
        assertEquals("test description", itemDto.getDescription());
        assertNotNull(itemDto.getCreatedDate());
        assertNotNull(itemDto.getDueDate());
        assertNull(itemDto.getCompletedDate());
        assertNotNull(itemDto.getLastModifiedDate());
        assertEquals(itemDto.getStatus(), ItemStatus.UNDONE);
    }

    @Test
    @SneakyThrows
    void testGettingItemById() {
        ItemDto item1 = new ItemDto();
        item1.setDescription("test description");
        item1.setStatus(ItemStatus.DONE);
        item1.setDueDate(LocalDateTime.parse("2023-11-05T11:50:55"));
        item1.setCompletedDate(LocalDateTime.parse("2023-11-04T11:50:55"));
        UUID itemId1 = itemService.createItem(item1);

        mockMvc.perform(get("/items/{id}", itemId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("test description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DONE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dueDate").value("2023-11-05T11:50:55"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completedDate").value("2023-11-04T11:50:55"));
    }


    private String asJsonString(final Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper getObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

}
