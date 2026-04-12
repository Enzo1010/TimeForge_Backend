package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.service.ScheduleExportService;
import br.com.timeforge.timeforge_api.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Validated
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleExportService scheduleExportService;

    @PostMapping("/generate/{turmaId}")
    public ResponseEntity<ScheduleGenerationResponseDTO> generate(@PathVariable @Positive(message = "turmaId deve ser maior que zero") Long turmaId) {
        ScheduleGenerationResponseDTO response = scheduleService.gerarHorario(new ScheduleGenerationRequestDTO(turmaId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<ScheduleGenerationResponseDTO> generate(@RequestBody @Valid ScheduleGenerationRequestDTO payload) {
        ScheduleGenerationResponseDTO response = scheduleService.gerarHorario(payload);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<ScheduleTurmaResponseDTO> consultarGradeTurma(@PathVariable @Positive(message = "turmaId deve ser maior que zero") Long turmaId) {
        ScheduleTurmaResponseDTO response = scheduleService.consultarGradeTurma(turmaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/turma/{turmaId}/export/pdf")
    public ResponseEntity<byte[]> exportarParaPDF(@PathVariable @Positive(message = "turmaId deve ser maior que zero") Long turmaId) {
        ScheduleTurmaResponseDTO schedule = scheduleService.consultarGradeTurma(turmaId);
        byte[] pdfBytes = scheduleExportService.exportarParaPDF(schedule);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grade_" + turmaId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/turma/{turmaId}/export/excel")
    public ResponseEntity<byte[]> exportarParaExcel(@PathVariable @Positive(message = "turmaId deve ser maior que zero") Long turmaId) {
        ScheduleTurmaResponseDTO schedule = scheduleService.consultarGradeTurma(turmaId);
        byte[] excelBytes = scheduleExportService.exportarParaExcel(schedule);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grade_" + turmaId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
