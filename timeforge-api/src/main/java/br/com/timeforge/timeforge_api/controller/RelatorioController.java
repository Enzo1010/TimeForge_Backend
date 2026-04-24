package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorDisciplinasResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorHorasResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeResponseDTO;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorDisciplinasExportService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorDisciplinasService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorHorasExportService;
import br.com.timeforge.timeforge_api.service.RelatorioProfessorHorasService;
import br.com.timeforge.timeforge_api.service.RelatorioSalasCapacidadeExportService;
import br.com.timeforge.timeforge_api.service.RelatorioSalasCapacidadeService;
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
    private final RelatorioSalasCapacidadeService relatorioSalasCapacidadeService;
    private final RelatorioSalasCapacidadeExportService relatorioSalasCapacidadeExportService;

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

    @GetMapping("/salas/capacidades")
    public ResponseEntity<RelatorioSalasCapacidadeResponseDTO> relatorioSalasCapacidades() {
        RelatorioSalasCapacidadeResponseDTO response = relatorioSalasCapacidadeService.gerarRelatorio();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/salas/capacidades/export/pdf")
    public ResponseEntity<byte[]> exportarRelatorioSalasCapacidadesPDF() {
        RelatorioSalasCapacidadeResponseDTO relatorio = relatorioSalasCapacidadeService.gerarRelatorio();
        byte[] pdfBytes = relatorioSalasCapacidadeExportService.exportarParaPDF(relatorio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_salas_capacidades.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
