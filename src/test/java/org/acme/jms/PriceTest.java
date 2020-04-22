package org.acme.jms;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
public class PriceTest {

    @SuppressWarnings("finally")
    @Test
    public void testLastPrice() throws Exception {
        try {
        assertTrue(Wait.waitFor(() -> {
            return RestAssured.given().when().get("/prices/last").getStatusCode() == 200;
        }, 3000, 25), "Price didnt became available in allotted time");
        } catch(AssertionFailedError afe) {
            try {
                printThreadDump();
            } finally {
                throw afe;
            }
        }

        RestAssured.given()
                .when().get("/prices/last")
                .then()
                .statusCode(200)
                .body(matchesPattern("\\d+"));
    }

    public void printThreadDump() {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

        for (ThreadInfo info : mbean.dumpAllThreads(true, true)) {
            System.out.println(System.lineSeparator() + "=============================");
            System.out.print(info.toString());
        }
        System.out.println("#############################");
    }
}
