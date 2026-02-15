package com.astra.school_service.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SwapRequest {
    private String standardA;
    private String divisionA;
    private String dayA;
    private int periodA;

    private String standardB;
    private String divisionB;
    private String dayB;
    private int periodB;

    public SwapRequest() {}

}
