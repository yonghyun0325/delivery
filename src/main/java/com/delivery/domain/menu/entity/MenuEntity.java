package com.delivery.domain.menu.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@Entity
@Table(
        name = "p_menu",
        indexes = {
                @Index(name = "idx_menu_store", columnList = "store_id, deleted_at"),
                @Index(name = "idx_menu_created", columnList = "created_at DESC")
        })
@Check(constraints = "price > 0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "is_hidden", nullable = false, columnDefinition = "boolean default false")
    private boolean hidden;

    public MenuEntity(UUID storeId, String name, String description, int price) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hidden = false;
    }

    public void update(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void updateHidden(boolean hidden) {
        this.hidden = hidden;
    }
}