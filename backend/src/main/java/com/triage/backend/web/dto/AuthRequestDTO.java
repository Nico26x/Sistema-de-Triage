package com.triage.backend.web.dto;

public record AuthRequestDTO(
    String email,
    String password
) {}