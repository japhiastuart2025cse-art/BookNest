-- BookNest MySQL Schema Definition for Books Table
CREATE TABLE IF NOT EXISTS books (
    book_id INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL
);

-- Members Table (Member 2 Integration)
CREATE TABLE IF NOT EXISTS members (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    department VARCHAR(100),
    role VARCHAR(50) DEFAULT 'Student',
    status VARCHAR(50) DEFAULT 'Active'
);

-- Circulation Table (Member 3: Circulation)
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

-- Fines Table (Member 3: Fines)
CREATE TABLE IF NOT EXISTS fines (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    issue_id INT NOT NULL UNIQUE,
    member_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    days_overdue INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_date DATE NULL,
    payment_note VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fines_circulation FOREIGN KEY (issue_id) REFERENCES circulation(issue_id) ON DELETE CASCADE,
    CONSTRAINT fk_fines_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE RESTRICT
);
