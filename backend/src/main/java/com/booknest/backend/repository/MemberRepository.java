package com.booknest.backend.repository;

import com.booknest.backend.model.Member;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Member> memberRowMapper = (rs, rowNum) -> new Member(
            rs.getInt("member_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("department"),
            rs.getString("role"),
            rs.getString("status")
    );

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Member> findAll() {
        String sql = "SELECT member_id, name, email, phone, department, role, status FROM members ORDER BY member_id ASC";
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    public Optional<Member> findById(int id) {
        String sql = "SELECT member_id, name, email, phone, department, role, status FROM members WHERE member_id = ?";
        List<Member> members = jdbcTemplate.query(sql, memberRowMapper, id);
        return members.stream().findFirst();
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM members WHERE member_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM members WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public Member save(Member member) {
        if (member.getId() != null && member.getId() > 0) {
            String sql = "INSERT INTO members (member_id, name, email, phone, department, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    sql,
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getDepartment(),
                    member.getRole() != null ? member.getRole() : "Student",
                    member.getStatus() != null ? member.getStatus() : "Active"
            );
            return member;
        } else {
            String sql = "INSERT INTO members (name, email, phone, department, role, status) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, member.getName());
                ps.setString(2, member.getEmail());
                ps.setString(3, member.getPhone());
                ps.setString(4, member.getDepartment());
                ps.setString(5, member.getRole() != null ? member.getRole() : "Student");
                ps.setString(6, member.getStatus() != null ? member.getStatus() : "Active");
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                member.setId(keyHolder.getKey().intValue());
            }
            return member;
        }
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
