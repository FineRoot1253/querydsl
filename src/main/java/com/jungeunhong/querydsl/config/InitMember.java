package com.jungeunhong.querydsl.config;

import com.jungeunhong.querydsl.member.command.domain.entity.Member;
import com.jungeunhong.querydsl.team.command.domain.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        // PostConstuctor와 Transactional은 함께 쓸수 없다.
        // 분리를 하고 따로 돌려야한다.
        @Transactional
        public void init(){
            Team teamA = new Team("team_a");
            Team teamB = new Team("team_b");

            em.persist(teamA);
            em.persist(teamB);

            StringBuilder builder = new StringBuilder("hong_");
            for (int i = 0; i < 100; i++) {
                builder.append(i);
                if(i%2 == 0){
                    em.persist(Member.createMember(builder.toString(), i, teamA));
                }else{
                    em.persist(Member.createMember(builder.toString(), i, teamB));
                }
                builder.delete(5,builder.length());
            }

        }
    }
}
