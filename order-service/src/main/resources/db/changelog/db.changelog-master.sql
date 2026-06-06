--liquibase formatted sql

--changeset autosalon-order:001-create-schema
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL
);

CREATE TABLE in_stock_car_orders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    client_id UUID NOT NULL REFERENCES app_users(id),
    manager_id UUID NOT NULL REFERENCES app_users(id),
    car_id UUID NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    status VARCHAR(64) NOT NULL
);

CREATE TABLE custom_car_orders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    client_id UUID NOT NULL REFERENCES app_users(id),
    manager_id UUID NOT NULL REFERENCES app_users(id),
    model_id UUID NOT NULL,
    total_price NUMERIC(14, 2) NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    status VARCHAR(64) NOT NULL
);

CREATE TABLE custom_order_components (
    custom_order_id UUID NOT NULL REFERENCES custom_car_orders(id),
    component_type VARCHAR(64) NOT NULL,
    component_option_id UUID NOT NULL,
    PRIMARY KEY (custom_order_id, component_type)
);

CREATE TABLE test_drive_requests (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    client_id UUID NOT NULL REFERENCES app_users(id),
    car_id UUID NOT NULL,
    starts_at TIMESTAMP NOT NULL
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(64) NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

--changeset autosalon-order:002-seed-users
INSERT INTO app_users (id, created_at, updated_at, removed, full_name, role) VALUES
('10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'egor poguliaev client', 'CLIENT'),
('10000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'egor poguliaev manager', 'DEALERSHIP_MANAGER'),
('10000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'egor poguliaev warehouse', 'WAREHOUSE_ADMIN'),
('10000000-0000-0000-0000-000000000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'egor poguliaev admin', 'SYSTEM_ADMIN'),
('10000000-0000-0000-0000-000000000005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'egor poguliaev other client', 'CLIENT');
