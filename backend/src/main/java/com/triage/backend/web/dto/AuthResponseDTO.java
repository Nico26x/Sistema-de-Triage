package com.triage.backend.web.dto;

public record AuthResponseDTO(
    String token,
    String tipo
) {}