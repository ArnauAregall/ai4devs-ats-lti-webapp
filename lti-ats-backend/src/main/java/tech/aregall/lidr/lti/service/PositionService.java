package tech.aregall.lidr.lti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.aregall.lidr.lti.domain.dto.CreatePositionRequest;
import tech.aregall.lidr.lti.domain.dto.UpdatePositionRequest;
import tech.aregall.lidr.lti.domain.entity.Position;
import tech.aregall.lidr.lti.infrastructure.persistence.PositionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public List<Position> findAll() {
        return StreamSupport.stream(positionRepository.findAll().spliterator(), false).toList();
    }

    public Position findById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
    }

    public Position create(CreatePositionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Position position = new Position(null, request.title(), request.department(), request.openDate(), now, now);
        return positionRepository.save(position);
    }

    public Position update(UUID id, UpdatePositionRequest request) {
        Position existing = findById(id);
        existing.setTitle(request.title());
        existing.setDepartment(request.department());
        existing.setOpenDate(request.openDate());
        existing.setUpdatedAt(LocalDateTime.now());
        return positionRepository.save(existing);
    }

    public void delete(UUID id) {
        findById(id);
        positionRepository.deleteById(id);
    }
}
