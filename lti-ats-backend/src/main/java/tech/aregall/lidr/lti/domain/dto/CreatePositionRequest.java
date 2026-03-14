package tech.aregall.lidr.lti.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePositionRequest(
        @NotBlank String title,
        @NotBlank String department,
        @NotNull LocalDate openDate) {
}
