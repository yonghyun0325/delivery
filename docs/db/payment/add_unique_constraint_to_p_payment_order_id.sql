-- Manual PostgreSQL migration.
-- This file is not executed automatically by Spring Boot, Flyway, Liquibase, or Docker init.
-- Check duplicate order_id values before applying.
-- Apply manually to the target PostgreSQL database.
-- Example:
--   psql -U <user> -d <database> -f docs/db/payment/add_unique_constraint_to_p_payment_order_id.sql
--
-- Pre-check duplicates:
-- SELECT order_id, COUNT(*)
-- FROM p_payment
-- GROUP BY order_id
-- HAVING COUNT(*) > 1;
--
-- Check existing constraint/index:
-- SELECT conname, contype, pg_get_constraintdef(oid)
-- FROM pg_constraint
-- WHERE conrelid = 'p_payment'::regclass
--   AND contype = 'u';

ALTER TABLE p_payment
ADD CONSTRAINT uk_payment_order_id UNIQUE (order_id);
