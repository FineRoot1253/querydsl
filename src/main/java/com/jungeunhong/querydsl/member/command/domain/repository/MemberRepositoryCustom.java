package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom{
    public List<MemberTeamDto> search(MemberSearchConditionDto condition);
    public Page<MemberTeamDto> searchPageSimple(MemberSearchConditionDto condition, Pageable pageable);
    public Page<MemberTeamDto> searchPageComplex(MemberSearchConditionDto condition, Pageable pageable);
}
