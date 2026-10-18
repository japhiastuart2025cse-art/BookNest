package com.booknest.backend.service;

import com.booknest.backend.exception.MemberNotFoundException;
import com.booknest.backend.model.Member;
import com.booknest.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(int id) {
        return memberRepository.findById(id);
    }

    public Member addMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member details must not be null.");
        }
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Member name cannot be blank.");
        }
        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Member email cannot be blank.");
        }
        if (memberRepository.existsByEmail(member.getEmail().trim())) {
            throw new IllegalArgumentException("Member with email " + member.getEmail() + " already exists.");
        }
        member.setName(member.getName().trim());
        member.setEmail(member.getEmail().trim());
        return memberRepository.save(member);
    }

    public void deleteMember(int id) {
        if (!memberRepository.existsById(id)) {
            throw new MemberNotFoundException("Member with ID " + id + " not found.");
        }
        memberRepository.deleteById(id);
    }
}
