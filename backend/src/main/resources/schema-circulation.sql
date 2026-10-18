-- =====================================================================
-- BookNest - Member 3: Circulation + Fines Schema Migration
-- =====================================================================
-- This optional script creates the required database tables for the
-- Circulation and Fines module. Run schema.sql for a complete new database.
--
-- Prerequisites: schema.sql (or the existing Books and Members modules)
-- must already have created books and members.
-- Tables:
--  1. circulation - Book loan transactions (issues & returns)
--  2. fines       - Overdue fine assessments and payment records
-- =====================================================================

-- 1. Circulation Table (Member 3: Issues & Returns)
CREATE TABLE IF NOT EXISTS circulation (
    issue_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT fk_circulation_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
    CONSTRAINT fk_circulation_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE RESTRICT
);

-- Index on active circulation lookups and overdue checks
CREATE INDEX idx_circulation_status ON circulation(status);
CREATE INDEX idx_circulation_due_date ON circulation(due_date);
CREATE INDEX idx_circulation_book ON circulation(book_id);
CREATE INDEX idx_circulation_member ON circulation(member_id);

-- 2. Fines Table (Member 3: Overdue Ledger & Payment Records)
CREATE TABLE IF NOT EXISTS fines (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    issue_id INT NOT NULL UNIQUE,
    member_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    days_overdue INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PAID'
    paid_date DATE NULL,
    payment_note VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fines_circulation FOREIGN KEY (issue_id) REFERENCES circulation(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_fines_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE RESTRICT
);

CREATE INDEX idx_fines_status ON fines(status);
CREATE INDEX idx_fines_member ON fines(member_id);
