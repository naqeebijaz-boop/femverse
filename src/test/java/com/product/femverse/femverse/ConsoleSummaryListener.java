package com.product.femverse.femverse;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class ConsoleSummaryListener implements ISuiteListener {

    @Override
    public void onFinish(ISuite suite) {
        int total = WomenPostmanRunner.testResults.size();

        long passed = WomenPostmanRunner.testResults.stream()
                .filter(r -> "PASSED".equals(r.get("status")))
                .count();

        long failed = WomenPostmanRunner.testResults.stream()
                .filter(r -> "FAILED".equals(r.get("status")))
                .count();

        long skipped = WomenPostmanRunner.testResults.stream()
                .filter(r -> "SKIPPED".equals(r.get("status")))
                .count();

        System.out.println("\n📊 Test Summary");
        System.out.println("• Total tests run: " + total);
        System.out.println("• ✅ Passes: " + passed);
        System.out.println("• ❌ Failures: " + failed);
        System.out.println("• ⏭️ Skipped: " + skipped);
    }
}
