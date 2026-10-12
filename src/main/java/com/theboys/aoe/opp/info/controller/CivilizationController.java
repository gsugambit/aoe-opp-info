package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.CivilizationDto;
import com.theboys.aoe.opp.info.service.Aoe4OppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/aoe4-opp/civilizations")
@RestController
@RequiredArgsConstructor
public class CivilizationController {

    private final Aoe4OppService aoe4OppService;

    @GetMapping
    public List<CivilizationDto> getCivilizations() {
        return aoe4OppService.getCivilizations();
    }
}
