package io.contexa.example.identityasep.dto;

public record ErrorResponse(
        String code,
        String message,
        String requestId
) {}
