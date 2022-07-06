package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import com.jungeunhong.querydsl.member.query.domain.dto.QMemberTeamDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.jungeunhong.querydsl.member.command.domain.entity.QMember.*;
import static com.jungeunhong.querydsl.team.command.domain.entity.QTeam.*;
import static org.springframework.util.StringUtils.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long memberId) {
        return Optional.of(em.find(Member.class, memberId));
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_querydsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m " +
                                "from Member m " +
                                "where m.username = :username",
                        Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_querydsl(String username) {
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchConditionDto conditionDto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(conditionDto.getUsername())) {
            builder.and(member.username.eq(conditionDto.getUsername()));
        }
        if (hasText(conditionDto.getTeamName())) {
            builder.and(team.name.eq(conditionDto.getTeamName()));
        }
        if (conditionDto.getAgeGoe() != null) {
            builder.and(member.age.goe(conditionDto.getAgeGoe()));
        }
        if (conditionDto.getAgeLoe() != null) {
            builder.and(member.age.loe(conditionDto.getAgeLoe()));
        }

        return queryFactory.select(
                        new QMemberTeamDto(
                                member.id.as("memberId"),
                                member.username,
                                member.age,
                                team.id.as("teamId"),
                                team.name.as("teamName")
                        ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }


    public List<MemberTeamDto> searchByWhereParam(MemberSearchConditionDto conditionDto) {
        return queryFactory.select(
                        new QMemberTeamDto(
                                member.id.as("memberId"),
                                member.username,
                                member.age,
                                team.id.as("teamId"),
                                team.name.as("teamName")
                        ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameCondition(conditionDto.getUsername()), teamNameCondition(conditionDto.getTeamName()), ageLoeCondition(conditionDto.getAgeLoe()), ageGoeCondition(conditionDto.getAgeGoe()))
                .fetch();
    }

    private Predicate parseCondition(MemberSearchConditionDto conditionDto) {
        return usernameCondition(conditionDto.getUsername()).and(teamNameCondition(conditionDto.getTeamName())).and(ageGoeCondition(conditionDto.getAgeGoe())).and(ageLoeCondition(conditionDto.getAgeLoe()));
    }

    private BooleanExpression usernameCondition(String usernameCond) {
        return hasText(usernameCond) ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression teamNameCondition(String teamNameCond) {
        return hasText(teamNameCond) ? team.name.eq(teamNameCond) : null;
    }

    private BooleanExpression ageGoeCondition(Integer ageGoeCond) {
        return ageGoeCond == null ? null : member.age.goe(ageGoeCond);
    }

    private BooleanExpression ageLoeCondition(Integer ageLoeCond) {
        return ageLoeCond == null ? null : member.age.loe(ageLoeCond);
    }


}
