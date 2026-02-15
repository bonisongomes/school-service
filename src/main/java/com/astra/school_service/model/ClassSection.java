package com.astra.school_service.model;

public record ClassSection(String standard, String division) {
    @Override
    public String toString() {
        return standard + division;
    }
}
