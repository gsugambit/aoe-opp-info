package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.service.Aoe4OppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/aoe4-opp")
@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final Aoe4OppService aoe4OppService;

    @GetMapping("/test")
    public void test() {
        aoe4OppService.collect();
    }
}

