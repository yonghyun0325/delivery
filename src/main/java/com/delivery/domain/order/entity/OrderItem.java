package com.delivery.domain.order.entity;

import com.delivery.common.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "p_order_item",
        indexes = {
            // 주문 상세 조회용
            @Index(name = "idx_order_item_order_id", columnList = "order_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private Integer menuPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal_price", nullable = false)
    private Integer subtotalPrice;

    public OrderItem(UUID menuId, String menuName, Integer menuPrice, Integer quantity) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
        this.subtotalPrice = menuPrice * quantity;
    }

    protected void setOrder(Order order) {
        this.order = order;
    }
}
