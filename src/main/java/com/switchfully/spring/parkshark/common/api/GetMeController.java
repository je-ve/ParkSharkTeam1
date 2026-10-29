package com.switchfully.spring.parkshark.common.api;


import com.switchfully.spring.parkshark.feature.security.domain.Feature;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GetMeController {

    @Secured("CREATE_MEMBER")
    @GetMapping
    public String HelloWorld() {
        return "Hello World!";
    }
}
