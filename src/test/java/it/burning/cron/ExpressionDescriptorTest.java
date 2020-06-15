package it.burning.cron;

import it.burning.cron.ExpressionParser.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionDescriptorTest {
    // Default options
    final Options options = new Options();

    @Test
    void getDescription() {
        // Test examples taken from (http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#examples)
        assertAll("Descriptions should match the expected results",
                  () -> assertEquals("At 12:00, every day", ExpressionDescriptor.getDescription("0 0 12 * * ?", options)),
                  () -> assertEquals("At 10:15, every day", ExpressionDescriptor.getDescription("0 15 10 ? * *", options)),
                  () -> assertEquals("At 10:15, every day", ExpressionDescriptor.getDescription("0 15 10 * * ?", options)),
                  () -> assertEquals("At 10:15, every day", ExpressionDescriptor.getDescription("0 15 10 * * ? *", options)),
                  () -> assertEquals("At 10:15, every day, only in 2005", ExpressionDescriptor.getDescription("0 15 10 * * ? 2005", options)),
                  () -> assertEquals("Every minute, between 14:00 and 14:59, every day", ExpressionDescriptor.getDescription("0 * 14 * * ?", options)),
                  () -> assertEquals("Every 5 minutes, between 14:00 and 14:59, every day", ExpressionDescriptor.getDescription("0 0/5 14 * * ?", options)),
                  () -> assertEquals("Every 5 minutes, at 14:00 and 18:00, every day", ExpressionDescriptor.getDescription("0 0/5 14,18 * * ?", options)),
                  () -> assertEquals("Every minute between 14:00 and 14:05, every day", ExpressionDescriptor.getDescription("0 0-5 14 * * ?", options)),
                  () -> assertEquals("At 10 and 44 minutes past the hour, at 14:00, every day, only on Wednesday, only in March", ExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED", options)),
                  () -> assertEquals("At 10:15, every day, Monday through Friday", ExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI", options)),
                  () -> assertEquals("At 10:15, on day 15 of the month", ExpressionDescriptor.getDescription("0 15 10 15 * ?", options)),
                  () -> assertEquals("At 10:15, on the last day of the month", ExpressionDescriptor.getDescription("0 15 10 L * ?", options)),
                  () -> assertEquals("At 10:15, 2 days before the last day of the month", ExpressionDescriptor.getDescription("0 15 10 L-2 * ?", options)),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month", ExpressionDescriptor.getDescription("0 15 10 ? * 6L", options)),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month, 2002 through 2005", ExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005", options)),
                  () -> assertEquals("At 10:15, every day, on the third Friday of the month", ExpressionDescriptor.getDescription("0 15 10 ? * 6#3", options)),
                  () -> assertEquals("At 12:00, every 5 days", ExpressionDescriptor.getDescription("0 0 12 1/5 * ?", options)),
                  () -> assertEquals("At 11:11, on day 11 of the month, only in November", ExpressionDescriptor.getDescription("0 11 11 11 11 ?", options))
        );
    }
}