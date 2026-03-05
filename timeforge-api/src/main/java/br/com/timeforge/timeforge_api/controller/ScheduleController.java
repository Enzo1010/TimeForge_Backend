package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/generate/{turmaId}")
    public ResponseEntity<String> generate(@PathVariable Long turmaId) {
        scheduleService.gerarHorario(turmaId);
        return ResponseEntity.ok("OK");
    }
}