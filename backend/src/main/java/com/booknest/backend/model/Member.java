package com.booknest.backend.model;

/**
 * Represents a registered library member (student, faculty, or staff).
 * Bridges Member 2's module to enable Member 3's circulation transactions.
 */
public class Member {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String role;     // e.g. "Student", "Faculty"
    private String status;   // e.g. "Active", "Suspended"

    public Member() {
    }

    public Member(Integer id, String name, String email, String phone, String department, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.role = role != null ? role : "Student";
        this.status = status != null ? status : "Active";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
