package tech.aregall.lidr.lti.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("position")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    @Id
    private UUID id;

    private String title;

    private String department;

    private LocalDate openDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
