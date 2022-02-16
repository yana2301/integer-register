package com.hazelcast.assignment;

import com.hazelcast.assignment.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("action")
public class ActionController {
    private final ActionRepository actionRepository;
    private final ActionService actionService;

    @Autowired
    public ActionController(ActionRepository actionRepository, ActionService actionService) {
        this.actionRepository = actionRepository;
        this.actionService = actionService;
    }

    @PostMapping("/add")
    public Integer addNumber(@RequestHeader("action-id") Long actionId, @RequestBody Integer value) {
        actionRepository.insertAction(actionId, value);
        return actionService.getCurrentValue();
    }

    @PostMapping("/add/compensate")
    public Integer revertAction(@RequestHeader("action-id") Long actionId) {
        actionRepository.insertCompensatingAction(actionId);
        return actionService.getCurrentValue();
    }

    @GetMapping("/get")
    public Integer getCurrentValue() {
        return actionService.getCurrentValue();
    }
}
