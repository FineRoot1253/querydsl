package com.jungeunhong.querydsl.member.presentation;

import com.jungeunhong.querydsl.common.dto.Result;
import com.jungeunhong.querydsl.member.command.domain.repository.MemberJpaRepository;
import com.jungeunhong.querydsl.member.command.domain.repository.MemberRepository;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchConditionDto memberSearchConditionDto){
        return memberJpaRepository.searchByWhereParam(memberSearchConditionDto);
    }

    @GetMapping("/v2/members")
    public Result<Page<MemberTeamDto>> searchMemberV2(MemberSearchConditionDto memberSearchConditionDto, Pageable pageable){
        return new Result<>(1,memberRepository.searchPageComplex(memberSearchConditionDto,pageable));
    }

}
