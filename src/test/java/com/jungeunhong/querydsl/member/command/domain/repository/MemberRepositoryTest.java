package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.command.domain.entity.QMember;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import com.jungeunhong.querydsl.team.command.domain.entity.Team;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void beforeEach() {
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
    @DisplayName("basicTest:[Success]")
    void basicTest(){
        //given
        Member hong1 = Member.createMember("hong_5", 10, null);
        memberRepository.save(hong1);

        //when
        log.info("hong_1 ID: {}",hong1.getId());
        Member member = memberRepository.findById(hong1.getId()).get();
        List<Member> all = memberRepository.findAll();
//        List<Member> all_querydsl = memberRepository.findAll_querydsl();
        List<Member> foundMemberByUsername = memberRepository.findByUsername(hong1.getUsername());
//        List<Member> foundMemberByUsernameWithQuerydsl = memberRepository.findByUsername_querydsl(hong1.getUsername());

        //then
        assertThat(member).isEqualTo(hong1);
        assertThat(foundMemberByUsername).containsExactly(hong1);
//        assertThat(foundMemberByUsernameWithQuerydsl).containsExactly(hong1);

        for (Member member1 : all) {
            log.info("Member: {}",member1);
        }
        log.info("--------------");
//        for (Member member1 : all_querydsl) {
//            log.info("Member: {}",member1);
//        }

    }

    @Test
    @DisplayName("searchTest:[Success]")
    void searchTest(){
        //given
        MemberSearchConditionDto memberSearchConditionDto = new MemberSearchConditionDto();
        memberSearchConditionDto.setAgeGoe(35);
        memberSearchConditionDto.setAgeLoe(40);
        memberSearchConditionDto.setTeamName("team_b");

        //when
//        List<MemberTeamDto> memberTeamDtos_1 = memberRepository.searchByBuilder(memberSearchConditionDto);
        List<MemberTeamDto> memberTeamDtos_2 = memberRepository.search(memberSearchConditionDto);

        //then
//        assertThat(memberTeamDtos_1).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");
        assertThat(memberTeamDtos_2).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");

    }

    @Test
    @DisplayName("searchTestSimple:[Success]")
    void searchTestSimple(){
        //given
        MemberSearchConditionDto memberSearchConditionDto = new MemberSearchConditionDto();
//        memberSearchConditionDto.setAgeGoe(35);
//        memberSearchConditionDto.setAgeLoe(40);
//        memberSearchConditionDto.setTeamName("team_b");


        //when
//        List<MemberTeamDto> memberTeamDtos_1 = memberRepository.searchByBuilder(memberSearchConditionDto);
        Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageSimple(memberSearchConditionDto, PageRequest.of(0, 3));

        //then
//        assertThat(memberTeamDtos_1).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");
//        assertThat(memberTeamDtos_2).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");
        assertThat(memberTeamDtos.getSize()).isEqualTo(3);
        assertThat(memberTeamDtos.getContent()).extracting("username").containsExactly("hong_1","hong_2","hong_3");
//        log.info("result: {}",memberTeamDtos.toString());
    }

    @Test
    @DisplayName("searchPredicateExecutorTest:[Success]")
    void searchPredicateExecutorTest(){
        //given
        BooleanExpression expression = QMember.member.age.between(20, 40);
        //when
        Iterable<Member> all = memberRepository.findAll(expression);
        //then
        for (Member member : all) {
            log.info("result: {}", member.toString());
        }
    }

}