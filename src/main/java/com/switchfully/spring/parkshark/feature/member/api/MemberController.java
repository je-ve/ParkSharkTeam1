package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDetailsDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register as a member")
    public MemberDetailsDto registerMember(@Valid @RequestBody RegisterMemberRequest request) {
        return memberService.registerMember(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get an overview of all members")
    @Secured("VIEW_MEMBERS")
    public List<MemberDto> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all the data of one member")
    @Secured("VIEW_MEMBERS")
    public MemberDetailsDto getMember(@PathVariable UUID id) {
        return memberService.getMember(id);
    }
}
