package com.task.todo.repository;

import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID>, JpaSpecificationExecutor<ItemEntity> {
    List<ItemEntity> findByStatus(ItemStatus itemStatus);

    List<ItemEntity> findAllByDueDateLessThanEqualAndStatus(LocalDateTime date, ItemStatus itemStatus);

    @Modifying
    @Query("UPDATE ItemEntity i SET i.status = :status WHERE i.id IN (:ids)")
    void updateItemsStatus(@Param("status") ItemStatus itemStatus, @Param("ids") List<UUID> itemIds);
}
