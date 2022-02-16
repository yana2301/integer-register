package com.hazelcast.assignment.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "actions")
@Getter
@Setter
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_id", unique = true)
    private Long actionId;

    @Column(name = "action_value")
    private Integer value;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;


}
