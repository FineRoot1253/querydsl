package com.jungeunhong.querydsl.member.query.domain.dto;

import lombok.Data;

@Data
public class MemberSearchConditionDto {

    // 회원 이름, 팀 이름, 나이 [ageGoe, ageLoe]
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

}
