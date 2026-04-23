package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorDisciplinasResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorHorasResponseDTO;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorDisciplinasExportService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorDisciplinasService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorHorasExportService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorHorasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioProfessorDisciplinasService relatorioProfessorDisciplinasService;
    private final RelatorioProfessorDisciplinasExportService relatorioProfessorDisciplinasExportService;
    private final RelatorioProfessorHorasService relatorioProfessorHorasService;
    private final RelatorioProfessorHorasExportService relatorioProfessorHorasExportService;

    @GetMapping("/professores/disciplinas")
    public ResponseEntity<RelatorioProfessorDisciplinasResponseDTO> relatorioDisciplinasProfessor() {
        RelatorioProfessorDisciplinasResponseDTO response = relatorioProfessorDisciplinasService.gerarRelatorio();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/professores/disciplinas/export/pdf")
    public ResponseEntity<byte[]> exportarRelatorioDisciplinasProfessorPDF() {
        RelatorioProfessorDisciplinasResponseDTO relatorio = relatorioProfessorDisciplinasService.gerarRelatorio();
        byte[] pdfBytes = relatorioProfessorDisciplinasExportService.exportarParaPDF(relatorio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_disciplinas_professor.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/professores/horas")
    public ResponseEntity<RelatorioProfessorHorasResponseDTO> relatorioHorasProfessor() {
        RelatorioProfessorHorasResponseDTO response = relatorioProfessorHorasService.gerarRelatorio();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/professores/horas/export/pdf")
    public ResponseEntity<byte[]> exportarRelatorioHorasProfessorPDF() {
        RelatorioProfessorHorasResponseDTO relatorio = relatorioProfessorHorasService.gerarRelatorio();
        byte[] pdfBytes = relatorioProfessorHorasExportService.exportarParaPDF(relatorio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_horas_professor.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
