package com.jungeunhong.querydsl;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.command.domain.entity.QMember;
import com.jungeunhong.querydsl.member.query.domain.entity.QTeam;
import com.jungeunhong.querydsl.member.query.domain.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.jungeunhong.querydsl.member.command.domain.entity.QMember.*;
import static com.jungeunhong.querydsl.member.query.domain.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory query;

    @BeforeEach
    void beforeEach() {
        query = new JPAQueryFactory(em);
        Team teamA = new Team("team_a");
        Team teamB = new Team("team_b");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.createMember("hong_1", 10, teamA);
        Member member2 = Member.createMember("hong_2", 20, teamA);

        Member member3 = Member.createMember("hong_3", 30, teamB);
        Member member4 = Member.createMember("hong_4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void jpqlTest() {
        Member findByJPQL = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "hong_1")
                .getSingleResult();

        assertThat(findByJPQL.getUsername()).isEqualTo("hong_1");

    }

    // 자동으로 PreparedStatement 방식으로 파라미터를 바인딩한다.
    @Test
    void querydslTest() {
        Member findMember = query.select(member)
                .from(member)
                .where(member.username.eq("hong_1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("hong_1");
    }


    @Test
    void search() {
        Member findMember = query.select(member)
                .from(member)
                .where(member.username.eq("hong_1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("hong_1");
    }

    @Test
    void searchAndParam() {
        Member findMember = query.select(member)
                .from(member)
                .where(
                        member.username.eq("hong_1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("hong_1");
    }

    @Test
    void resultFetch() {
//        List<Member> fetchResult = query.selectFrom(member)
//                .fetch();
//
//        Member fetchOneResult = query.selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirstResult = query.selectFrom(member)
//                .fetchFirst();

//        QueryResults<Member> fetchResults = query.selectFrom(member).fetchResults();
//        fetchResults.getTotal();

        long count = query.selectFrom(member).fetchCount();
//        assertThat(findMember.getUsername()).isEqualTo("hong_1");
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원이름이 없을시 맨 마지막으로 배열 (nulls last 라는 옵션임)
     */
    @Test
    void sort() {
        em.persist(Member.createMember(null, 100, null));
        em.persist(Member.createMember("hong_5", 100, null));
        em.persist(Member.createMember("hong_6", 100, null));

        List<Member> members = query.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member5.getUsername()).isEqualTo("hong_5");
        assertThat(member6.getUsername()).isEqualTo("hong_6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging_1() {
//        List<Member> members = query.selectFrom(member)
//                .orderBy(member.username.desc())
//                .offset(1)
//                .limit(2)
//                .fetch();

        Long count = query.select(member.count())// member
                .from(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchOne();

//        assertThat(members.size()).isEqualTo(2);
        assertThat(count).isEqualTo(4);

    }

    @Test
    void aggregation() {
        List<Tuple> list = query.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = list.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     * 각 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    void aggregation_groupBy(){
        List<Tuple> tuples = query.select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = tuples.get(0);
        Tuple teamB = tuples.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("team_a");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("team_b");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

}
