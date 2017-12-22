package io.galeb.core.entity;

import java.util.Map;

public interface WithStatus {

    void setStatus(Map<Long, Status> status);

    enum Status {
        PENDING,
        OK,
        UNKNOWN,
        DELETED
    }

    Map<Long, Status> getStatus();

}
