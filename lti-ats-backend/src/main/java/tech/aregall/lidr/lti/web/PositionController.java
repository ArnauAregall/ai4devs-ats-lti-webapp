package tech.aregall.lidr.lti.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.aregall.lidr.lti.domain.dto.CreatePositionRequest;
import tech.aregall.lidr.lti.domain.dto.PositionResponse;
import tech.aregall.lidr.lti.domain.dto.UpdatePositionRequest;
import tech.aregall.lidr.lti.service.PositionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    List<PositionResponse> getAll() {
        return positionService.findAll().stream()
                .map(PositionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    PositionResponse getById(@PathVariable UUID id) {
        return PositionResponse.from(positionService.findById(id));
    }

    @PostMapping
    ResponseEntity<PositionResponse> create(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PositionResponse.from(positionService.create(request)));
    }

    @PutMapping("/{id}")
    PositionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePositionRequest request) {
        return PositionResponse.from(positionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
