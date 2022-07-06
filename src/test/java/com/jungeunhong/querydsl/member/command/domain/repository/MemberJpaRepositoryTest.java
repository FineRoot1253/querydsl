package com.jungeunhong.querydsl.member.command.domain.repository;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberSearchConditionDto;
import com.jungeunhong.querydsl.member.query.domain.dto.MemberTeamDto;
import com.jungeunhong.querydsl.team.command.domain.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

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
        Member hong1 = Member.createMember("hong_1", 10, null);
        memberJpaRepository.save(hong1);

        //when
        log.info("hong_1 ID: {}",hong1.getId());
        Member member = memberJpaRepository.findById(hong1.getId()).get();
        List<Member> all = memberJpaRepository.findAll();
        List<Member> all_querydsl = memberJpaRepository.findAll_querydsl();
        List<Member> foundMemberByUsername = memberJpaRepository.findByUsername(hong1.getUsername());
        List<Member> foundMemberByUsernameWithQuerydsl = memberJpaRepository.findByUsername_querydsl(hong1.getUsername());

        //then
        assertThat(member).isEqualTo(hong1);
        assertThat(foundMemberByUsername).containsExactly(hong1);
        assertThat(foundMemberByUsernameWithQuerydsl).containsExactly(hong1);

        for (Member member1 : all) {
            log.info("Member: {}",member1);
        }
            log.info("--------------");
        for (Member member1 : all_querydsl) {
            log.info("Member: {}",member1);
        }
        
    }

    @Test
    @DisplayName("searchTest:[success]")
    void searchTest(){
        //given
        MemberSearchConditionDto memberSearchConditionDto = new MemberSearchConditionDto();
        memberSearchConditionDto.setAgeGoe(35);
        memberSearchConditionDto.setAgeLoe(40);
        memberSearchConditionDto.setTeamName("team_b");

        //when
        List<MemberTeamDto> memberTeamDtos_1 = memberJpaRepository.searchByBuilder(memberSearchConditionDto);
        List<MemberTeamDto> memberTeamDtos_2 = memberJpaRepository.searchByWhereParam(memberSearchConditionDto);

        //then
        assertThat(memberTeamDtos_1).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");
        assertThat(memberTeamDtos_2).extracting(MemberTeamDto::getUsername).containsExactly("hong_4");
    }
    
    
}