ALTER TABLE p_payment
ADD CONSTRAINT uk_payment_order_id UNIQUE (order_id);
