package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/generate/{turmaId}")
    public ResponseEntity<ScheduleGenerationResponseDTO> generate(@PathVariable Long turmaId) {
        ScheduleGenerationResponseDTO response = scheduleService.gerarHorario(turmaId);
        return ResponseEntity.ok(response);
    }
}
