package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeItemDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeResponseDTO;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class RelatorioSalasCapacidadeExportService {

    public byte[] exportarParaPDF(RelatorioSalasCapacidadeResponseDTO relatorio) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            adicionarCabecalho(document, relatorio);
            adicionarTabela(document, relatorio.getItens());
            adicionarRodape(document);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao exportar relatorio de salas e capacidades", e);
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void adicionarCabecalho(Document document, RelatorioSalasCapacidadeResponseDTO relatorio) {
        Paragraph titulo = new Paragraph("Relatorio de salas e capacidades")
                .setBold()
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f);
        document.add(titulo);

        Paragraph dataGeracao = new Paragraph(
                "Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8f);
        document.add(dataGeracao);

        Paragraph resumo = new Paragraph(
                "Salas: " + safe(relatorio.getTotalSalas()) +
                        " | Capacidade total: " + safe(relatorio.getCapacidadeTotal()) +
                        " | Capacidade media: " + safe(relatorio.getCapacidadeMedia())
        )
                .setFontSize(11f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16f);
        document.add(resumo);
    }

    private void adicionarTabela(Document document, List<RelatorioSalasCapacidadeItemDTO> itens) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 2, 2})).useAllAvailableWidth();

        String[] headers = {"Sala", "Tipo", "Capacidade"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setBold());
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        if (itens == null || itens.isEmpty()) {
            Cell cell = new Cell(1, 3)
                    .add(new Paragraph("Nenhuma sala encontrada."))
                    .setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
            document.add(table);
            return;
        }

        for (RelatorioSalasCapacidadeItemDTO item : itens) {
            table.addCell(new Cell().add(new Paragraph(safe(item.getSalaNome()))));
            table.addCell(new Cell().add(new Paragraph(safe(item.getTipoSala())))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(safe(item.getCapacidade()))))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        document.add(table);
    }

    private void adicionarRodape(Document document) {
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

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private String safe(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }
}
