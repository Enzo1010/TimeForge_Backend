package br.com.timeforge.timeforge_api.service;
import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleExportService {

    private static final String[] DIAS_SEMANA = {
            "Segunda-feira", "Terça-feira", "Quarta-feira",
            "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"
    };

    /**
     * Exporta a grade de horários para PDF.
     */
    public byte[] exportarParaPDF(ScheduleTurmaResponseDTO schedule, char formato) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Define orientação com base no parâmetro
            PageSize pageSize = (formato == 'P' || formato == 'p')
                    ? PageSize.A4.rotate()   // Paisagem
                    : PageSize.A4;           // Retrato (padrão)

            Document document = new Document(pdfDoc, pageSize);

            adicionarCabecalhoPDF(document, schedule);
            adicionarTabelaPDF(document, schedule.getAulas());
            adicionarRodapePDF(document);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao exportar para PDF", e);
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Exporta a grade de horários para Excel.
     */
    public byte[] exportarParaExcel(ScheduleTurmaResponseDTO schedule) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Grade de Horários");

            // Configurar largura das colunas
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 3000);

            // Cabeçalho
            adicionarCabecalhoExcel(sheet, schedule);

            // Dados
            adicionarDadosExcel(sheet, schedule.getAulas());

            // Rodapé
            adicionarRodapeExcel(sheet, schedule.getAulas());

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao exportar para Excel", e);
            throw new RuntimeException("Erro ao gerar Excel: " + e.getMessage(), e);
        }
    }

    private void adicionarCabecalhoPDF(Document document, ScheduleTurmaResponseDTO schedule) {
        // Título
        Paragraph titulo = new Paragraph("Grade de Horários")
                .setBold()
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f);
        document.add(titulo);

        // Informações da turma
        Paragraph infoTurma = new Paragraph("Turma: " + schedule.getTurmaNome())
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5f);
        document.add(infoTurma);

        Paragraph dataGeracao = new Paragraph(
                "Gerada em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15f);
        document.add(dataGeracao);

        Paragraph totalAulas = new Paragraph("Total de Aulas: " + schedule.getTotalAulas())
                .setFontSize(11f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f);
        document.add(totalAulas);
    }

    private void adicionarTabelaPDF(Document document, List<ScheduleAulaResponseDTO> aulas) {
        Table table = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();

        // Cabeçalhos da tabela
        String[] headers = {"Dia", "Horário", "Disciplina", "Professor", "Sala"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setBold());
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        // Ordenar aulas por dia e horário
        List<ScheduleAulaResponseDTO> aulasOrdenadas = aulas.stream()
                .sorted((a1, a2) -> {
                    int dia1 = a1.getDiaSemana() != null ? a1.getDiaSemana().getValue() : 6;
                    int dia2 = a2.getDiaSemana() != null ? a2.getDiaSemana().getValue() : 6;
                    int diaCmp = Integer.compare(dia1, dia2);
                    if (diaCmp != 0) return diaCmp;
                    
                    java.time.LocalTime hora1 = a1.getHoraInicio() != null ? a1.getHoraInicio() : java.time.LocalTime.MAX;
                    java.time.LocalTime hora2 = a2.getHoraInicio() != null ? a2.getHoraInicio() : java.time.LocalTime.MAX;
                    return hora1.compareTo(hora2);
                })
                .toList();

        // Dados das aulas
        for (ScheduleAulaResponseDTO aula : aulasOrdenadas) {
            String dia = obterNomeDia(aula.getDiaSemana());
            String horario = formatarHorario(aula.getHoraInicio(), aula.getHoraFim());

            table.addCell(new Cell().add(new Paragraph(dia)));
            table.addCell(new Cell().add(new Paragraph(horario)));
            table.addCell(new Cell().add(new Paragraph(aula.getDisciplinaNome() != null ? aula.getDisciplinaNome() : "-")));
            table.addCell(new Cell().add(new Paragraph(aula.getProfessorNome() != null ? aula.getProfessorNome() : "-")));
            table.addCell(new Cell().add(new Paragraph(aula.getSalaNome() != null ? aula.getSalaNome() : "-")));
        }

        document.add(table);
    }

    private void adicionarRodapePDF(Document document) {
        Paragraph rodape = new Paragraph()
                .setMarginTop(20f)
                .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(0.5f))
                .setPaddingTop(10f)
                .add(new Paragraph("Documento gerado automaticamente pelo TimeForge - " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                        .setFontSize(9f)
                        .setTextAlignment(TextAlignment.CENTER));
        document.add(rodape);
    }

    private void adicionarCabecalhoExcel(Sheet sheet, ScheduleTurmaResponseDTO schedule) {
        Row row1 = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell cell1 = row1.createCell(0);
        cell1.setCellValue("Grade de Horários");
        CellStyle styleTitulo = sheet.getWorkbook().createCellStyle();
        styleTitulo.setFont(criarFonteBold(sheet.getWorkbook()));
        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
        cell1.setCellStyle(styleTitulo);

        Row row2 = sheet.createRow(1);
        org.apache.poi.ss.usermodel.Cell cell2 = row2.createCell(0);
        cell2.setCellValue("Turma: " + schedule.getTurmaNome());

        Row row3 = sheet.createRow(2);
        org.apache.poi.ss.usermodel.Cell cell3 = row3.createCell(0);
        cell3.setCellValue("Gerada em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        Row row4 = sheet.createRow(3);
        org.apache.poi.ss.usermodel.Cell cell4 = row4.createCell(0);
        cell4.setCellValue("Total de Aulas: " + schedule.getTotalAulas());

        // Linha em branco
        sheet.createRow(4);

        // Cabeçalho da tabela
        Row headerRow = sheet.createRow(5);
        String[] headers = {"Dia", "Horário", "Disciplina", "Professor", "Sala"};
        CellStyle styleHeader = sheet.getWorkbook().createCellStyle();
        styleHeader.setFont(criarFonteBold(sheet.getWorkbook()));
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
        styleHeader.setFillForegroundColor((short) 22);
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cellHeader = headerRow.createCell(i);
            cellHeader.setCellValue(headers[i]);
            cellHeader.setCellStyle(styleHeader);
        }
    }

    private void adicionarDadosExcel(Sheet sheet, List<ScheduleAulaResponseDTO> aulas) {
        // Ordenar aulas por dia e horário
        List<ScheduleAulaResponseDTO> aulasOrdenadas = aulas.stream()
                .sorted((a1, a2) -> {
                    int dia1 = a1.getDiaSemana() != null ? a1.getDiaSemana().getValue() : 6;
                    int dia2 = a2.getDiaSemana() != null ? a2.getDiaSemana().getValue() : 6;
                    int diaCmp = Integer.compare(dia1, dia2);
                    if (diaCmp != 0) return diaCmp;
                    
                    java.time.LocalTime hora1 = a1.getHoraInicio() != null ? a1.getHoraInicio() : java.time.LocalTime.MAX;
                    java.time.LocalTime hora2 = a2.getHoraInicio() != null ? a2.getHoraInicio() : java.time.LocalTime.MAX;
                    return hora1.compareTo(hora2);
                })
                .toList();

        int rowNum = 6;
        for (ScheduleAulaResponseDTO aula : aulasOrdenadas) {
            Row row = sheet.createRow(rowNum++);

            String dia = obterNomeDia(aula.getDiaSemana());
            String horario = formatarHorario(aula.getHoraInicio(), aula.getHoraFim());

            row.createCell(0).setCellValue(dia);
            row.createCell(1).setCellValue(horario);
            row.createCell(2).setCellValue(aula.getDisciplinaNome() != null ? aula.getDisciplinaNome() : "-");
            row.createCell(3).setCellValue(aula.getProfessorNome() != null ? aula.getProfessorNome() : "-");
            row.createCell(4).setCellValue(aula.getSalaNome() != null ? aula.getSalaNome() : "-");
        }
    }

    private void adicionarRodapeExcel(Sheet sheet, List<ScheduleAulaResponseDTO> aulas) {
        int lastRow = 6 + aulas.size();
        Row rowRodape = sheet.createRow(lastRow + 1);
        org.apache.poi.ss.usermodel.Cell cellRodape = rowRodape.createCell(0);
        cellRodape.setCellValue("Documento gerado automaticamente pelo TimeForge - " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        CellStyle styleRodape = sheet.getWorkbook().createCellStyle();
        Font fonteRodape = sheet.getWorkbook().createFont();
        fonteRodape.setFontHeightInPoints((short) 9);
        styleRodape.setFont(fonteRodape);
        styleRodape.setAlignment(HorizontalAlignment.CENTER);
        cellRodape.setCellStyle(styleRodape);
    }

    private Font criarFonteBold(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        return font;
    }

    private String obterNomeDia(java.time.DayOfWeek diaSemana) {
        if (diaSemana == null) {
            return "-";
        }
        return DIAS_SEMANA[diaSemana.getValue() - 1];
    }

    private String formatarHorario(java.time.LocalTime horaInicio, java.time.LocalTime horaFim) {
        if (horaInicio == null || horaFim == null) {
            return "--:-- - --:--";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return horaInicio.format(formatter) + " - " + horaFim.format(formatter);
    }
}
