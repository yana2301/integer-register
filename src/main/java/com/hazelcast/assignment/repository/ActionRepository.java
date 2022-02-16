package com.hazelcast.assignment.repository;

import com.hazelcast.assignment.model.Action;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ActionRepository extends CrudRepository<Action, Long> {

    @Query("SELECT SUM(a.value) FROM Action a WHERE a.isActive = true")
    Optional<Integer> getCurrentValue();

    @Modifying
    @Query(value = "INSERT INTO actions (action_id, is_active) VALUES(:action_id, false) ON CONFLICT(action_id) DO UPDATE SET is_active = false", nativeQuery = true)
    @Transactional
    void insertCompensatingAction(@Param(value = "action_id") long actionId);

    @Modifying
    @Query(value = "INSERT INTO actions (action_id, action_value, is_active) VALUES(:action_id, :action_value, true) ON CONFLICT(action_id) DO UPDATE SET action_value = :action_value", nativeQuery = true)
    @Transactional
    void insertAction(@Param(value = "action_id") long actionId, @Param(value = "action_value") int actionValue);

}
