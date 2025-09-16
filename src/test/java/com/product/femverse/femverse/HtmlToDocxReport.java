package com.product.femverse.femverse;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;

import org.testng.*;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class HtmlToDocxReport implements ITestListener, ISuiteListener {

    private XWPFDocument document;
    private List<String> passedTests = new ArrayList<>();
    private List<String> failedTests = new ArrayList<>();
    private List<String> skippedTests = new ArrayList<>();

    // Report file name
    private final String reportFileName = "Femverse_API_Report.docx";

    // Full report path (Jenkins WORKSPACE if available, else local project root)
    private String reportPath;

    @Override
    public void onStart(ISuite suite) {
        document = new XWPFDocument();

        // ✅ Determine where to save
        String workspace = System.getenv("WORKSPACE");
        if (workspace == null || workspace.isEmpty()) {
            workspace = System.getProperty("user.dir");  // fallback for local run
        }
        reportPath = workspace + File.separator + reportFileName;

        // 🗑️ Delete old report if exists
        File oldFile = new File(reportPath);
        if (oldFile.exists()) {
            oldFile.delete();
        }

        // ✅ Fix layout
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11900)); // A4 width
        pageSize.setH(BigInteger.valueOf(16840)); // A4 height

        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(720));
        pageMar.setBottom(BigInteger.valueOf(720));
        pageMar.setLeft(BigInteger.valueOf(1000));
        pageMar.setRight(BigInteger.valueOf(1000));

        // 📄 Cover Page Header
        XWPFParagraph cover = document.createParagraph();
        cover.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = cover.createRun();
        run.setText("📑 Femverse API Test Report");
        run.setFontSize(20);
        run.setBold(true);
        run.addBreak();

        // Metadata
        XWPFParagraph meta = document.createParagraph();
        meta.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun metaRun = meta.createRun();
        metaRun.setFontSize(12);
        metaRun.setText("👨‍💻 MadeBy: Naqeeb Ejaz");
        metaRun.addBreak();
        metaRun.setText("🏢 Company: Imagination AI");
        metaRun.addBreak();
        metaRun.setText("📌 Designation: Sr.SQA");
        metaRun.addBreak();
        metaRun.setText("📅 Date: " + new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()));
        metaRun.addBreak();
        metaRun.addBreak();

        // Page Break after cover
        document.createParagraph().setPageBreak(true);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        int statusCode = (result.getAttribute("statusCode") != null) ? (int) result.getAttribute("statusCode") : 200;
        passedTests.add("✅ " + result.getMethod().getMethodName() + " → PASSED | Status Code: " + statusCode);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        int statusCode = (result.getAttribute("statusCode") != null) ? (int) result.getAttribute("statusCode") : 500;
        failedTests.add("❌ " + result.getMethod().getMethodName() +
                " → FAILED | Status Code: " + statusCode +
                " | Error: " + result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skippedTests.add("⚠️ " + result.getMethod().getMethodName() + " → SKIPPED");
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            if (!passedTests.isEmpty()) {
                addSectionHeading("✅ Passed Tests");
                for (String test : passedTests) addBulletPoint(test);
                document.createParagraph().setPageBreak(true);
            }

            if (!failedTests.isEmpty()) {
                addSectionHeading("❌ Failed Tests");
                for (String test : failedTests) addBulletPoint(test);
                document.createParagraph().setPageBreak(true);
            }

            if (!skippedTests.isEmpty()) {
                addSectionHeading("⚠️ Skipped Tests");
                for (String test : skippedTests) addBulletPoint(test);
                document.createParagraph().setPageBreak(true);
            }

            // ✅ Save file inside Jenkins WORKSPACE or project root
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
}
