package tech.aregall.lidr.lti.service;

import java.util.UUID;

public class PositionNotFoundException extends RuntimeException {

    public PositionNotFoundException(UUID id) {
        super("Position not found with id: " + id);
    }
}
