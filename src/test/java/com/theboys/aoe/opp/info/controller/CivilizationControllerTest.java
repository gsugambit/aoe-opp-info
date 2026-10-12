package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.dto.CivilizationDto;
import com.theboys.aoe.opp.info.service.Aoe4OppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CivilizationControllerTest {

    @Mock
    private Aoe4OppService aoe4OppService;

    @InjectMocks
    private CivilizationController civilizationController;

    @Test
    public void testGetCivilizations() {
        // given
        final List<CivilizationDto> civList = List.of(
                CivilizationDto.builder().withValue("abbasid_dynasty").withDisplayName("Abbasid Dynasty").build()
        );
        when(aoe4OppService.getCivilizations()).thenReturn(civList);

        // when
        final List<CivilizationDto> result = civilizationController.getCivilizations();

        // then
        assertSame(civList, result);
        verify(aoe4OppService).getCivilizations();
    }
}
