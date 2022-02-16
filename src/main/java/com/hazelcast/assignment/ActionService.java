package com.hazelcast.assignment;

import com.hazelcast.assignment.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ActionService {
    private final ActionRepository actionRepository;

    @Autowired
    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    /**
     * To speed up current value calculations we could:
     * 1) add timestamp row to the actions table. Index timestamp field. It would indicate time of the latest row update.
     * We will use it as a version of the database.
     * <p>
     * Getting current value strategy:
     * <p>
     * 1) When request current value from the database for the first time- cache it and latest timestamp present in actions table.
     * 2) When request current value next time - first request latest timestamp, and only if it differs with the cached value - request sum calculation.
     * <p>
     * Updating values strategy:
     * In case of conflict for the add action(ActionRepository.insertAction) - do not update timestamp field.
     * cache latest value retrieved from the database.
     */
    public int getCurrentValue() {
        return actionRepository.getCurrentValue().orElse(0);
    }
}
