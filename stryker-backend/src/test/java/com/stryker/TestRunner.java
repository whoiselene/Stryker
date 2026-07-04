package com.stryker;

import com.stryker.engine.PhysicsTest;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                     STRYKER PHYSICS CORE UNIT TESTS                            ");
        System.out.println("================================================================================");

        PhysicsTest testSuite = new PhysicsTest();
        int passed = 0;
        int failed = 0;

        // Test 1: Intercepts
        try {
            System.out.print("[TEST] Running testIntercepts... ");
            testSuite.testIntercepts();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED!");
            t.printStackTrace();
            failed++;
        }

        // Test 2: Apply Friction
        try {
            System.out.print("[TEST] Running testApplyFriction... ");
            testSuite.testApplyFriction();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED!");
            t.printStackTrace();
            failed++;
        }

        // Test 3: Constrain To Bounds (Player)
        try {
            System.out.print("[TEST] Running testConstrainToBoundsPlayer... ");
            testSuite.testConstrainToBoundsPlayer();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED!");
            t.printStackTrace();
            failed++;
        }

        // Test 4: Constrain To Bounds (Ball Bounce)
        try {
            System.out.print("[TEST] Running testConstrainToBoundsBallBounce... ");
            testSuite.testConstrainToBoundsBallBounce();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED!");
            t.printStackTrace();
            failed++;
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(String.format("RESULTS: %d tests passed, %d tests failed.", passed, failed));
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
