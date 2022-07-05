package com.jungeunhong.querydsl;

import com.jungeunhong.querydsl.member.command.domain.dto.MemberDto;
import com.jungeunhong.querydsl.member.command.domain.dto.QMemberDto;
import com.jungeunhong.querydsl.member.command.domain.dto.UserDto;
import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.command.domain.entity.QMember;
import com.jungeunhong.querydsl.team.command.domain.entity.QTeam;
import com.jungeunhong.querydsl.team.command.domain.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.jungeunhong.querydsl.member.command.domain.entity.QMember.*;
import static com.jungeunhong.querydsl.team.command.domain.entity.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
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
    void aggregation_groupBy() {
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

    /**
     * 조건: 팀 A에 소속된 모든 회원
     */
    @Test
    @DisplayName("join_1:success")
    void join_1() {
        //given
        List<Member> members = query.selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("team_a"))
                .fetch();
        //when

        //then
        assertThat(members).extracting("username").containsExactly("hong_1", "hong_2");
    }

    @Test
    @DisplayName("theta_join:Success")
    void theta_join() {
        //given
        em.persist(Member.createMember("team_a", 10, null));
        em.persist(Member.createMember("team_b", 10, null));
        //when
        List<Member> members = query.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(members)
                .extracting("username")
                .containsExactly("team_a", "team_b");

    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 team_a인 팀만 조인, 회원은 모두 조회 할 것
     * JPQL: select m from Member m left join m.team t on t.name = 'team_a'
     */
    @Test
    @DisplayName("on_filtering_join:success")
    void on_filtering_join() {
        //given
        List<Tuple> tuples = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("team_a"))
                .fetch();
        //when
        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
        //then

    }

    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원 이름이 팀 이름과 같은 대상 외부조인
     */
    @Test
    @DisplayName("on_no_relation_join:success")
    void on_no_relation_join() {
        //given
        em.persist(Member.createMember("team_a", 10, null));
        em.persist(Member.createMember("team_b", 10, null));
        em.persist(Member.createMember("team_c", 10, null));
        //when
        List<Tuple> tuples = query.select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();
        //then
        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("noFetch_join:[success]")
    void noFetch_join() {
        //given
        em.flush(); // DB에 영속성 적용
        em.clear(); // Persistence Context Clear

        //when
        Member findMember = query.select(member)
                .from(member)
                .where(member.username.eq("hong_1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //then
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("fetch_join:[success]")
    void fetch_join() {
        //given
        em.flush(); // DB에 영속성 적용
        em.clear(); // Persistence Context Clear

        //when
        Member findMember = query.select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(QMember.member.username.eq("hong_1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //then
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    /**
     * [중첩 서브쿼리, Nested SubQuery]
     * 나이가 가장 많은 회원 조회
     */
    @Test
    @DisplayName("subQuery:[success]")
    void subQuery() {
        //given
        QMember subMember1 = new QMember("sub_member_1");

        //when
        List<Member> members = query.selectFrom(member)
                .where(member.age.eq(
                        select(subMember1.age.max()).from(subMember1)
                )).fetch();
        //then
        assertThat(members).extracting("age").containsExactly(40);

    }

    /**
     * [중첩 서브쿼리, Nested SubQuery]
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    @DisplayName("subQuery_Goe:[success]")
    void subQuery_Goe() {
        //given
        QMember subMember1 = new QMember("sub_member_1");

        //when
        List<Member> members = query.selectFrom(member)
                .where(member.age.goe(
                        select(subMember1.age.avg()).from(subMember1)
                )).fetch();
        //then
        assertThat(members).extracting("age").containsExactly(30, 40);

    }

    /**
     * [중첩 서브쿼리, Nested SubQuery]
     * In 절 활용 예시
     * 이렇게 쓰면 안된다 그냥 보여주는 예시이다.
     */
    @Test
    @DisplayName("subQuery_In:[success]")
    void subQuery_In() {
        //given
        QMember subMember1 = new QMember("sub_member_1");

        //when
        List<Member> members = query.selectFrom(member)
                .where(member.age.in(
                        select(subMember1.age)
                                .from(subMember1)
                                .where(subMember1.age.gt(10))
                )).fetch();
        //then
        assertThat(members).extracting("age").containsExactly(20, 30, 40);
    }

    /**
     * [스칼라 서브쿼리, Scalar SubQuery]
     * select절 서브 쿼리
     */
    @Test
    @DisplayName("selectSubQuery:[success]")
    void selectSubQuery() {
        //given
        QMember subMember1 = new QMember("sub_member_1");

        //when
        List<Tuple> tuples = query.select(
                        member.username,
                        select(subMember1.age.avg())
                                .from(subMember1))
                .from(member)
                .fetch();
        //then
//        assertThat(tuples).extracting("age").containsExactly(20,30,40);
        for (Tuple tuple : tuples) {
            log.info("tuple: {}", tuple);
        }
    }

    /**
     * 단순 조건
     */
    @Test
    void basicCase() {
        List<String> fetch = query
                .select(
                        member.age
                                .when(10).then("열살")
                                .when(20).then("스무살")
                                .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : fetch) {
            log.info("result: {}", s);
        }
    }

    /**
     * 복잡한 조건
     */
    @Test
    @DisplayName("complexCase:[success]")
    void complexCase() {
        //given
        List<String> fetch = query
                .select(
                        new CaseBuilder()
                                .when(member.age.between(0, 20)).then("0~20살")
                                .when(member.age.between(30, 40)).then("30~40살")
                                .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : fetch) {
            log.info("result: {}", s);
        }
    }

    /**
     *
     */
    @Test
    @DisplayName("constant:[success]")
    void constant() {
        //given
        List<Tuple> tuples = query.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        //when
        for (Tuple tuple : tuples) {
            log.info("tuple: {}", tuple);
        }

    }

    @Test
    @DisplayName("concatenations:[success]")
    void concatenations() {
        //given
        List<String> members = query.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("hong_1"))
                .fetch();

        //when
        for (String member : members) {
            log.info("member: {}", member);
        }

    }

    @Test
    @DisplayName("projection_one:[success]")
    void projection_one() {
        //given
        List<String> members = query.select(member.username)
                .from(member)
                .fetch();

        //given
        List<Member> members2 = query.select(member)
                .from(member)
                .fetch();
        //when
        for (String member : members) {
            log.info("member: {}", member);
        }

        for (Member member : members2) {
            log.info("member: {}", member);
        }

    }

    @Test
    @DisplayName("tupleProjection:[success]")
    void tupleProjection() {
        //given
        List<Tuple> tuples = query.select(member.username, member.age)
                .from(member)
                .fetch();
        //when
        for (Tuple member : tuples) {
            log.info("member: {}", member);
        }

    }

    @Test
    @DisplayName("jpaDtoProjectionByNew:[success]")
    void jpaDtoProjectionByNew() {
        //given
        List<MemberDto> memberDtos = em.createQuery("select " +
                                "new com.jungeunhong.querydsl.member.command.domain.dto.MemberDto(m.username, m.age) " +
                                "from Member m",
                        MemberDto.class)
                .getResultList();
        //when
        for (MemberDto memberDto : memberDtos) {
            log.info("member: {}", memberDto);
        }

    }

    @Test
    @DisplayName("queryDslDtoProjectionBySetter:[success]")
    void queryDslDtoProjectionBySetter() {
        //given
        List<MemberDto> memberDtos = query.select(Projections.bean(
                        MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : memberDtos) {
            log.info("member: {}", memberDto);
        }

    }

    @Test
    @DisplayName("queryDslDtoProjectionByfields:[success]")
    void queryDslDtoProjectionByfields() {
        //given
        List<MemberDto> memberDtos = query.select(Projections.fields(
                        MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : memberDtos) {
            log.info("member: {}", memberDto);
        }
    }

    @Test
    @DisplayName("queryDslDtoProjectionByConstructor:[success]")
    void queryDslDtoProjectionByConstructor() {
        //given
        List<MemberDto> memberDtos = query.select(Projections.constructor(
                        MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : memberDtos) {
            log.info("member: {}", memberDto);
        }

    }

    @Test
    @DisplayName("queryDslUserDtoProjectionByfields:[success]")
    void queryDslUserDtoProjectionByfields() {
        //given
        List<UserDto> userDtos = query.select(Projections.fields(
                        UserDto.class,
                        member.username.as("name"),
                        member.age
                ))
                .from(member)
                .fetch();
        //when
        for (UserDto userDto : userDtos) {
            log.info("member: {}", userDto);
        }
    }

    @Test
    @DisplayName("queryDslUserDtoProjectionByfieldsWithSubQuery:[success]")
    void queryDslUserDtoProjectionByfieldsWithSubQuery() {
        //given
        QMember subMember1 = new QMember("sub_member_1");
        List<UserDto> userDtos = query.select(Projections.fields(
                        UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(select(subMember1.age.max()).from(subMember1), "age")
                ))
                .from(member)
                .fetch();
        //when
        for (UserDto userDto : userDtos) {
            log.info("member: {}", userDto);
        }
    }

    @Test
    @DisplayName("queryDslQDtoProjection:[success]")
    void queryDslQDtoProjection() {
        //given
        QMember subMember1 = new QMember("sub_member_1");
        List<MemberDto> userDtos = query.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (MemberDto userDto : userDtos) {
            log.info("member: {}", userDto);
        }
    }

    @Test
    @DisplayName("dynamicQuery_BooleanBuilder:[success]")
    void dynamicQuery_BooleanBuilder() {
        //given
        String usernameParam = "hong_1";
        Integer ageParam = 10;

        //when
        List<Member> result = searchMember_1(usernameParam, ageParam);
        // then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("dynamicQuery_BooleanBuilder:[success]")
    void dynamicQuery_WhereParam() {
        //given
        String usernameParam = "hong_1";
        Integer ageParam = 10;

        //when
//        List<Member> result = searchMember_2(usernameParam, ageParam);
        List<Member> result = searchMember_3(usernameParam, ageParam);
        // then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember_1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return null;
    }

    private List<Member> searchMember_2(String usernameParam, Integer ageParam) {
        return query.selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    private List<Member> searchMember_3(String usernameParam, Integer ageParam) {
        return query.selectFrom(member)
                .where(allEq(usernameParam,ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam == null ? null : member.username.eq(usernameParam);
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam == null ? null :member.age.eq(ageParam);
    }

    private Predicate allEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

}
