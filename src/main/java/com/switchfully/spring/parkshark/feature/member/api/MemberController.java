package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(path = "members")
public class MemberController {

    private MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register as a user")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Member is created", useReturnTypeSchema = true)
    public ResponseEntity<MemberDto> registerAsMember(@Valid @RequestBody RegisterMemberRequest request) {
        MemberDto registeredUser = memberService.RegisterUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredUser.id())
                .toUri();
        return ResponseEntity.created(location).body(registeredUser);
    }
}




