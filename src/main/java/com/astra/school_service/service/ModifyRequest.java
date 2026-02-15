package com.astra.school_service.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModifyRequest {
    private String standard;
    private String division;
    private String day;
    private int period;
    private String subject;
    private String teacher;

    public ModifyRequest() {}

}
