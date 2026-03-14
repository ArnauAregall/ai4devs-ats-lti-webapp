package tech.aregall.lidr.lti.infrastructure.persistence;

import org.springframework.data.repository.CrudRepository;
import tech.aregall.lidr.lti.domain.entity.Position;

import java.util.UUID;

public interface PositionRepository extends CrudRepository<Position, UUID> {
}
