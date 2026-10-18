package com.booknest.backend.repository;

import com.booknest.backend.model.CirculationRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CirculationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CirculationRecord> enrichedRowMapper = (rs, rowNum) -> {
        CirculationRecord record = new CirculationRecord();
        record.setIssueId(rs.getInt("issue_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setMemberId(rs.getInt("member_id"));

        Date issueSqlDate = rs.getDate("issue_date");
        if (issueSqlDate != null) record.setIssueDate(issueSqlDate.toLocalDate());

        Date dueSqlDate = rs.getDate("due_date");
        if (dueSqlDate != null) record.setDueDate(dueSqlDate.toLocalDate());

        Date returnSqlDate = rs.getDate("return_date");
        if (returnSqlDate != null) record.setReturnDate(returnSqlDate.toLocalDate());

        record.setStatus(rs.getString("status"));

        // Enriched fields from JOINs
        record.setBookTitle(rs.getString("book_title"));
        record.setBookAuthor(rs.getString("book_author"));
        record.setMemberName(rs.getString("member_name"));
        record.setMemberEmail(rs.getString("member_email"));
        record.setMemberDepartment(rs.getString("member_dept"));

        int fineId = rs.getInt("fine_id");
        if (!rs.wasNull()) {
            record.setFineId(fineId);
            record.setFineAmount(rs.getDouble("fine_amount"));
            record.setFineStatus(rs.getString("fine_status"));
        } else {
            record.setFineStatus("NONE");
        }

        return record;
    };

    public CirculationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SELECT = """
            SELECT c.issue_id, c.book_id, c.member_id, c.issue_date, c.due_date, c.return_date, c.status,
                   b.title AS book_title, b.author AS book_author,
                   m.name AS member_name, m.email AS member_email, m.department AS member_dept,
                   f.fine_id, f.amount AS fine_amount, f.status AS fine_status
            FROM circulation c
            JOIN books b ON c.book_id = b.book_id
            JOIN members m ON c.member_id = m.member_id
            LEFT JOIN fines f ON c.issue_id = f.issue_id
            """;

    public List<CirculationRecord> findAll() {
        String sql = BASE_SELECT + " ORDER BY c.issue_id DESC";
        return jdbcTemplate.query(sql, enrichedRowMapper);
    }

    public Optional<CirculationRecord> findById(int issueId) {
        String sql = BASE_SELECT + " WHERE c.issue_id = ?";
        List<CirculationRecord> records = jdbcTemplate.query(sql, enrichedRowMapper, issueId);
        return records.stream().findFirst();
    }

    public List<CirculationRecord> findActiveLoans() {
        String sql = BASE_SELECT + " WHERE c.status = 'ISSUED' ORDER BY c.issue_id DESC";
        return jdbcTemplate.query(sql, enrichedRowMapper);
    }

    public List<CirculationRecord> findOverdueLoans(LocalDate today) {
        String sql = BASE_SELECT + " WHERE c.status = 'ISSUED' AND c.due_date < ? ORDER BY c.due_date ASC";
        return jdbcTemplate.query(sql, enrichedRowMapper, Date.valueOf(today));
    }

    public boolean existsById(int issueId) {
        String sql = "SELECT COUNT(*) FROM circulation WHERE issue_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, issueId);
        return count != null && count > 0;
    }

    public CirculationRecord save(CirculationRecord record) {
        String sql = "INSERT INTO circulation (book_id, member_id, issue_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, record.getBookId());
            ps.setInt(2, record.getMemberId());
            ps.setDate(3, Date.valueOf(record.getIssueDate()));
            ps.setDate(4, Date.valueOf(record.getDueDate()));
            if (record.getReturnDate() != null) {
                ps.setDate(5, Date.valueOf(record.getReturnDate()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.setString(6, record.getStatus() != null ? record.getStatus() : "ISSUED");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            record.setIssueId(keyHolder.getKey().intValue());
        }

        return record;
    }

    public int markReturned(int issueId, LocalDate returnDate) {
        String sql = "UPDATE circulation SET status = 'RETURNED', return_date = ? WHERE issue_id = ? AND status = 'ISSUED'";
        return jdbcTemplate.update(sql, Date.valueOf(returnDate), issueId);
    }

    public int deleteById(int issueId) {
        String sql = "DELETE FROM circulation WHERE issue_id = ?";
        return jdbcTemplate.update(sql, issueId);
    }
}
