package com.theboys.aoe.opp.info.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class ExceptionResponse {

    private String message;
    private String exceptionClass;
}
