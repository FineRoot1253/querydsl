package com.jungeunhong.querydsl.member.command.domain.entity;

import com.jungeunhong.querydsl.member.query.domain.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    void testEntity(){
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

        em.flush();
        em.clear();

        List<Member> resultList = em.createQuery("select m from Member m", Member.class).getResultList();
        for (Member member
                : resultList) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }
    }

}