CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    tenure_type TEXT NOT NULL,
    tenure_value INTEGER NOT NULL,
    grace_period INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS product_fees (
    id SERIAL PRIMARY KEY,
    type TEXT NOT NULL,
    calculation_type TEXT NOT NULL,
    amount NUMERIC(10, 2),
    apply_at_origination BOOLEAN,
    product_id INTEGER NOT NULL REFERENCES products(id),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    full_name TEXT,
    email TEXT,
    phone TEXT,
    credit_score INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS loans(
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id),
    product_id INTEGER REFERENCES products(id),
    tenure_type TEXT,
    tenure_value INTEGER,
    principal_amount NUMERIC(10, 2),
    outstanding_balance NUMERIC(10, 2),
    total_fees_applied NUMERIC(10, 2),
    disbursed_amount NUMERIC(10, 2),
    origination_date TIMESTAMP,
    due_date TIMESTAMP,
    type TEXT,
    status TEXT,
    grace_period INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS Loan_fees (
    id SERIAL PRIMARY KEY,
    loan_id INTEGER REFERENCES loans(id),
    type TEXT,
    calculation_type TEXT,
    amount NUMERIC(10, 2),
    applied_at_origination BOOLEAN,
    percentage DECIMAL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS loan_installments (
    id SERIAL PRIMARY KEY,
    loan_id INTEGER REFERENCES loans(id),
    due_date TIMESTAMP,
    amount_due NUMERIC(10, 2),
    amount_paid NUMERIC(10, 2),
    status TEXT,
    paid_date TIMESTAMP,
    late_fee_amount NUMERIC(10, 2),
    late_fee_applied BOOLEAN
);

CREATE TABLE IF NOT EXISTS payments(
    id SERIAL PRIMARY KEY,
    loan_id INTEGER REFERENCES loans(id),
    installment_id INTEGER REFERENCES loan_installments(id),
    amount NUMERIC(10, 2),
    payment_date TIMESTAMP,
    method TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payment_allocations(
    id SERIAL PRIMARY KEY,
    payment_id INTEGER REFERENCES paymentS(id),
    loan_installment_id INTEGER REFERENCES loan_installments(id),
    amount NUMERIC(10, 2)
);