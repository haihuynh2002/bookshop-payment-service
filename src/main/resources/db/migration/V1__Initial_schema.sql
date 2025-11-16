-- Create payment table
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_id VARCHAR(100),
    token VARCHAR(100),
    amount DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    status VARCHAR(255) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    payment_method VARCHAR(255),
    
    billing_street VARCHAR(50),
    billing_city VARCHAR(50),
    billing_state VARCHAR(50),
    billing_postal_code VARCHAR(50),
    billing_country VARCHAR(50),
    
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Additional constraints for data integrity
    CONSTRAINT chk_payment_amount_positive CHECK (amount > 0),
    CONSTRAINT uq_payment_payment_id UNIQUE (payment_id)
);

-- Create indexes for better performance
CREATE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_payment_user_id ON payment(user_id);
CREATE INDEX idx_payment_payment_id ON payment(payment_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_payment_method ON payment(payment_method);
CREATE INDEX idx_payment_created_date ON payment(created_date);
CREATE INDEX idx_payment_email ON payment(email);