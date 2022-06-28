package com.jungeunhong.querydsl.member.command.domain.entity;

import com.jungeunhong.querydsl.common.entity.BaseEntity;
import com.jungeunhong.querydsl.member.query.domain.entity.Team;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(of={"id","username","age"})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username, int age){
        this.username=username;
        this.age=age;
    }

    public static Member createMember(String username, int age, Team team){
        Member member = new Member(username, age);
        if(team != null){
            member.changeTeam(team);
            return member;
        }
        return member;
    }

    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }

}
