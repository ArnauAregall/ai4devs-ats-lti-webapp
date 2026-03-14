package tech.aregall.lidr.lti.domain.dto;

import tech.aregall.lidr.lti.domain.entity.Position;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PositionResponse(
        UUID id,
        String title,
        String department,
        LocalDate openDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PositionResponse from(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getTitle(),
                position.getDepartment(),
                position.getOpenDate(),
                position.getCreatedAt(),
                position.getUpdatedAt());
    }
}
