package com.switchfully.spring.parkshark.feature.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    @Query("select m from Member m join fetch m.credentials order by m.lastName, m.firstName")
    List<Member> findAllWithCredentials();
}
