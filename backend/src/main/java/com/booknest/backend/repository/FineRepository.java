package com.booknest.backend.repository;

import com.booknest.backend.model.Fine;
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
public class FineRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Fine> enrichedFineRowMapper = (rs, rowNum) -> {
        Fine fine = new Fine();
        fine.setFineId(rs.getInt("fine_id"));
        fine.setIssueId(rs.getInt("issue_id"));
        fine.setMemberId(rs.getInt("member_id"));
        fine.setAmount(rs.getDouble("amount"));
        fine.setDaysOverdue(rs.getInt("days_overdue"));
        fine.setStatus(rs.getString("status"));

        Date paidSqlDate = rs.getDate("paid_date");
        if (paidSqlDate != null) fine.setPaidDate(paidSqlDate.toLocalDate());

        fine.setPaymentNote(rs.getString("payment_note"));

        // Enriched info
        fine.setBookTitle(rs.getString("book_title"));
        fine.setMemberName(rs.getString("member_name"));
        fine.setMemberEmail(rs.getString("member_email"));

        Date dueSqlDate = rs.getDate("due_date");
        if (dueSqlDate != null) fine.setDueDate(dueSqlDate.toLocalDate());

        Date returnSqlDate = rs.getDate("return_date");
        if (returnSqlDate != null) fine.setReturnDate(returnSqlDate.toLocalDate());

        return fine;
    };

    public FineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SELECT = """
            SELECT f.fine_id, f.issue_id, f.member_id, f.amount, f.days_overdue, f.status, f.paid_date, f.payment_note,
                   b.title AS book_title,
                   m.name AS member_name, m.email AS member_email,
                   c.due_date, c.return_date
            FROM fines f
            JOIN circulation c ON f.issue_id = c.issue_id
            JOIN books b ON c.book_id = b.book_id
            JOIN members m ON f.member_id = m.member_id
            """;

    public List<Fine> findAll() {
        String sql = BASE_SELECT + " ORDER BY f.fine_id DESC";
        return jdbcTemplate.query(sql, enrichedFineRowMapper);
    }

    public Optional<Fine> findById(int fineId) {
        String sql = BASE_SELECT + " WHERE f.fine_id = ?";
        List<Fine> fines = jdbcTemplate.query(sql, enrichedFineRowMapper, fineId);
        return fines.stream().findFirst();
    }

    public Optional<Fine> findByIssueId(int issueId) {
        String sql = BASE_SELECT + " WHERE f.issue_id = ?";
        List<Fine> fines = jdbcTemplate.query(sql, enrichedFineRowMapper, issueId);
        return fines.stream().findFirst();
    }

    public List<Fine> findByStatus(String status) {
        String sql = BASE_SELECT + " WHERE f.status = ? ORDER BY f.fine_id DESC";
        return jdbcTemplate.query(sql, enrichedFineRowMapper, status);
    }

    public Fine save(Fine fine) {
        String sql = "INSERT INTO fines (issue_id, member_id, amount, days_overdue, status, paid_date, payment_note) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, fine.getIssueId());
            ps.setInt(2, fine.getMemberId());
            ps.setDouble(3, fine.getAmount());
            ps.setInt(4, fine.getDaysOverdue());
            ps.setString(5, fine.getStatus() != null ? fine.getStatus() : "PENDING");
            if (fine.getPaidDate() != null) {
                ps.setDate(6, Date.valueOf(fine.getPaidDate()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setString(7, fine.getPaymentNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            fine.setFineId(keyHolder.getKey().intValue());
        }

        return fine;
    }

    public int update(Fine fine) {
        String sql = "UPDATE fines SET amount = ?, days_overdue = ?, status = ?, paid_date = ?, payment_note = ? WHERE fine_id = ?";
        return jdbcTemplate.update(
                sql,
                fine.getAmount(),
                fine.getDaysOverdue(),
                fine.getStatus(),
                fine.getPaidDate() != null ? Date.valueOf(fine.getPaidDate()) : null,
                fine.getPaymentNote(),
                fine.getFineId()
        );
    }

    public int markPaid(int fineId, LocalDate paidDate, String paymentNote) {
        String sql = "UPDATE fines SET status = 'PAID', paid_date = ?, payment_note = ? WHERE fine_id = ?";
        return jdbcTemplate.update(sql, Date.valueOf(paidDate), paymentNote, fineId);
    }

    public int deleteById(int fineId) {
        String sql = "DELETE FROM fines WHERE fine_id = ?";
        return jdbcTemplate.update(sql, fineId);
    }
}
