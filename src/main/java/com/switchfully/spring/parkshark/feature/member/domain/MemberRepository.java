package com.switchfully.spring.parkshark.feature.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

}
