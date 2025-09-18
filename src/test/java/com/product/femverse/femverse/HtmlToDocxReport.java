package com.product.femverse.femverse;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class HtmlToDocxReport implements ISuiteListener {

    private XWPFDocument document;
    private final String reportFileName = "Femverse_API_Report.docx";
    private String reportPath;

    @Override
    public void onStart(ISuite suite) {
        document = new XWPFDocument();

        // Determine save location
        String workspace = System.getenv("WORKSPACE");
        if (workspace == null || workspace.isEmpty()) {
            workspace = System.getProperty("user.dir");  // fallback
        }
        reportPath = workspace + File.separator + reportFileName;

        // Delete old file
        File oldFile = new File(reportPath);
        if (oldFile.exists()) oldFile.delete();

        // Page layout
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11900));
        pageSize.setH(BigInteger.valueOf(16840));
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(720));
        pageMar.setBottom(BigInteger.valueOf(720));
        pageMar.setLeft(BigInteger.valueOf(1000));
        pageMar.setRight(BigInteger.valueOf(1000));

        // Cover page
        XWPFParagraph cover = document.createParagraph();
        cover.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = cover.createRun();
        run.setText("📑 Femverse API Test Report");
        run.setFontSize(20);
        run.setBold(true);
        run.addBreak();

        XWPFParagraph meta = document.createParagraph();
        meta.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun metaRun = meta.createRun();
        metaRun.setFontSize(12);
        metaRun.setText("👨‍💻 Made By: Naqeeb Ejaz");
        metaRun.addBreak();
        metaRun.setText("🏢 Company: Imagination AI");
        metaRun.addBreak();
        metaRun.setText("📌 Designation: Sr.SQA");
        metaRun.addBreak();
        metaRun.setText("📅 Date: " + new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()));
        metaRun.addBreak();
        metaRun.addBreak();

       // document.createParagraph().setPageBreak(true);
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            // Fetch results from WomenPostmanRunner
            for (Map<String, Object> test : WomenPostmanRunner.testResults) {
                addSectionHeading("➡️ Running: " + test.get("testName"));
                addBulletPoint("   Method: " + test.get("method"));
                addBulletPoint("   URL: " + test.get("url"));

                // Add colored status line
                addBulletPointWithStatus((String) test.get("status"), (int) test.get("statusCode"), (String) test.get("response"));

                document.createParagraph().setPageBreak(false);
            }

            // Save Word report
            try (FileOutputStream out = new FileOutputStream(reportPath)) {
                document.write(out);
            }
            document.close();
            System.out.println("✅ Word report generated at: " + reportPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSectionHeading(String text) {
        XWPFParagraph para = document.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setFontSize(16);
        run.setText(text);
        run.addBreak();
    }

    private void addBulletPoint(String text) {
        XWPFParagraph para = document.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        para.setStyle("ListBullet");
        XWPFRun run = para.createRun();
        run.setFontSize(12);
        run.setText(text);
    }

    // New method to add colored PASSED/FAILED with response
    private void addBulletPointWithStatus(String status, int statusCode, String response) {
        XWPFParagraph para = document.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        para.setStyle("ListBullet");
        XWPFRun run = para.createRun();
        run.setFontSize(12);

        if ("PASSED".equalsIgnoreCase(status)) {
            run.setColor("008000"); // Green for PASSED
            run.setText("✅ PASSED | Status Code: " + statusCode);
        } else {
            run.setColor("FF0000"); // Red for FAILED
            run.setText("❌ FAILED | Status Code: " + statusCode);
        }

        // Response as a separate bullet point
        XWPFParagraph responsePara = document.createParagraph();
        responsePara.setAlignment(ParagraphAlignment.LEFT);
        responsePara.setStyle("ListBullet");
        XWPFRun responseRun = responsePara.createRun();
        responseRun.setFontSize(12);
        responseRun.setText("Response: " + response);
    }
}
