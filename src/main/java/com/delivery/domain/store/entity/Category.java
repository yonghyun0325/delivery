package com.delivery.domain.store.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public void update(String name) {
        this.name = name;
    }
}
