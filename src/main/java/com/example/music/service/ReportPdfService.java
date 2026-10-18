package com.example.music.service;

import com.example.music.dto.Report;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Renders ALL reports (1, 6 and 12 months, ending with the month of the chosen date) into one PDF. */
@Service
public class ReportPdfService {
    private static final int[] PERIODS = {1, 6, 12};
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM.yyyy");
    private static final Color HEAD = new Color(0xE9, 0xEB, 0xF7), LINE = new Color(0xC8, 0xCC, 0xE0);

    private final ReportService reports;
    private final BaseFont regular, bold; // embedded TTFs: the built-in PDF fonts have no Cyrillic

    public ReportPdfService(ReportService reports) throws IOException {
        this.reports = reports;
        this.regular = load("DejaVuSans.ttf");
        this.bold = load("DejaVuSans-Bold.ttf");
    }

    public byte[] generate(LocalDate date) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 48, 56);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override public void onEndPage(PdfWriter w, Document d) {
                    ColumnText.showTextAligned(w.getDirectContent(), Element.ALIGN_CENTER,
                            new Phrase("Стр. " + w.getPageNumber(), font(regular, 9, Color.GRAY)),
                            (d.left() + d.right()) / 2, d.bottom() - 24, 0);
                }
            });
            doc.open();
            doc.add(new Paragraph("Счетоводни справки (отчети)", font(bold, 20, Color.BLACK)));
            Paragraph sub = new Paragraph("Към " + DAY.format(date) + " · създадено на " + DAY.format(LocalDate.now()),
                    font(regular, 10, Color.GRAY));
            sub.setSpacingAfter(4);
            doc.add(sub);
            boolean first = true;
            for (int months : PERIODS) {
                if (!first) doc.newPage();
                first = false;
                section(doc, reports.build(date, months));
            }
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
        return out.toByteArray();
    }

    private void section(Document doc, Report r) throws DocumentException {
        Paragraph h = new Paragraph("Отчет за " + r.months() + (r.months() == 1 ? " месец" : " месеца"), font(bold, 14, Color.BLACK));
        h.setSpacingBefore(12);
        doc.add(h);
        doc.add(new Paragraph("Период: " + DAY.format(r.from()) + " – " + DAY.format(r.to()), font(regular, 10, Color.GRAY)));
        Paragraph sum = new Paragraph("Събития: " + r.eventCount() + "     Събрани пари: " + money(r.totalRevenue()), font(bold, 11, Color.BLACK));
        sum.setSpacingBefore(4);
        doc.add(sum);

        PdfPTable monthly = new PdfPTable(new float[]{2, 1.5f, 2});
        setup(monthly);
        row(monthly, 1, true, "Месец", "Събития", "Събрано");
        for (var m : r.monthly())
            row(monthly, 1, false, YearMonth.parse(m.month()).format(MONTH), String.valueOf(m.events()), money(m.revenue()));
        row(monthly, 1, true, "Общо", String.valueOf(r.eventCount()), money(r.totalRevenue()));
        doc.add(monthly);

        if (r.events().isEmpty()) {
            Paragraph none = new Paragraph("Няма събития за този период.", font(regular, 10, Color.GRAY));
            none.setSpacingBefore(10);
            doc.add(none);
            return;
        }
        Paragraph title = new Paragraph("Събития", font(bold, 12, Color.BLACK));
        title.setSpacingBefore(12);
        doc.add(title);
        PdfPTable events = new PdfPTable(new float[]{1.3f, 3.2f, 1f, 1.3f, 1.7f});
        setup(events);
        row(events, 2, true, "Дата", "Събитие", "Билети", "Цена", "Събрано");
        for (var e : r.events())
            row(events, 2, false, DAY.format(e.date()), e.title(), String.valueOf(e.ticketsSold()), money(e.ticketPrice()), money(e.revenue()));
        doc.add(events);
    }

    private static void setup(PdfPTable t) {
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setHeaderRows(1);
    }

    /** First {@code leftCols} columns are left-aligned (text), the rest right-aligned (numbers). */
    private void row(PdfPTable t, int leftCols, boolean head, String... values) {
        for (int i = 0; i < values.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(values[i], font(head ? bold : regular, 9.5f, Color.BLACK)));
            c.setHorizontalAlignment(i < leftCols ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            c.setPadding(5);
            c.setBorderColor(LINE);
            if (head) c.setBackgroundColor(HEAD);
            t.addCell(c);
        }
    }

    private static Font font(BaseFont base, float size, Color color) { return new Font(base, size, Font.NORMAL, color); }

    private static String money(BigDecimal v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator(' ');
        s.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", s).format(v) + " €";
    }

    private static BaseFont load(String name) throws IOException {
        try (InputStream in = ReportPdfService.class.getResourceAsStream("/fonts/" + name)) {
            if (in == null) throw new IllegalStateException("Missing font on classpath: /fonts/" + name);
            return BaseFont.createFont(name, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, in.readAllBytes(), null);
        } catch (DocumentException e) {
            throw new IOException("Cannot load font " + name, e);
        }
    }
}
