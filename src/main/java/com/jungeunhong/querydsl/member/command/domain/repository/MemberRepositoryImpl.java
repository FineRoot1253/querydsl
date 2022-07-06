package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import com.jungeunhong.querydsl.member.query.domain.dto.QMemberTeamDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.jungeunhong.querydsl.member.command.domain.entity.QMember.*;
import static com.jungeunhong.querydsl.team.command.domain.entity.QTeam.*;

@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(MemberSearchConditionDto condition) {
        return queryFactory.select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
        )).from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()))
                .fetch();
    }

    /// 영한님의 예제는 simple에선 fetchResults를 사용하고
    /// Complex에서 fetch(), fetchCount()를 사용하시지만
    /// 지금 querydsl에서 권장하는 방식은 그냥 이렇게 따로 보내는것을 권장하고
    /// 영한님이 소개해주신 방식은 지금 deprecate상태이다.
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchConditionDto condition, Pageable pageable) {
        List<MemberTeamDto> memberTeamDtos = queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageLoe(condition.getAgeLoe()),
                ageGoe(condition.getAgeGoe()))
                .fetchOne();

        return new PageImpl<>(memberTeamDtos, pageable, totalCount);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchConditionDto condition, Pageable pageable) {
        List<MemberTeamDto> memberTeamDtos = queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> longJPAQuery = queryFactory.select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()));

        return PageableExecutionUtils.getPage(memberTeamDtos,pageable, longJPAQuery::fetchOne);
    }


    private Predicate usernameEq(String usernameCond) {
        return StringUtils.hasText(usernameCond) ? member.username.eq(usernameCond) : null;
    }

    private Predicate teamNameEq(String teamNameCond) {
        return StringUtils.hasText(teamNameCond) ? team.name.eq(teamNameCond) : null;
    }

    private Predicate ageLoe(Integer ageLoeCond) {
        return ageLoeCond != null ? member.age.loe(ageLoeCond) : null;
    }

    private Predicate ageGoe(Integer ageGoeCond) {
        return ageGoeCond != null ? member.age.goe(ageGoeCond) : null;
    }
}
