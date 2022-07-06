package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.command.domain.entity.QMember;
import com.jungeunhong.querydsl.member.command.domain.repository.support.Querydsl4RepositorySupport;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.jungeunhong.querydsl.member.command.domain.entity.QMember.*;
import static com.jungeunhong.querydsl.team.command.domain.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {
    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchConditionDto condition, Pageable pageable) {
        JPAQuery<Member> jpaQuery = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));
        List<Member> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();

        return PageableExecutionUtils.getPage(content, pageable, jpaQuery::fetchCount);
    }

//    public Page<Member> searchPageByApplyPage_2(MemberSearchConditionDto condition, Pageable pageable) {
//        return applyPagination(pageable, query -> query
//                .selectFrom(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe())));
//    }

    public Page<Member> searchPageByApplyPage_3(MemberSearchConditionDto condition, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())),
                countQuery -> countQuery
                        .select(member.count())
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe()))
        );
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
