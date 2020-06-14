package com.burning.cron;

import com.burning.cron.ExpressionParser.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionDescriptorTest {
    @Test
    void getDescription() {
        final String expression = "0 59 23 31 DEC 2,4 /2";
        final Options options = new Options();
        final String description = ExpressionDescriptor.getDescription(expression, options);

        assertEquals("At 23:59, on day 31 of the month, only on Monday and Wednesday, only in December, every 2 years", description, "Description should match the expected result");
        System.out.println(expression + " -> \"" + description + "\"");
    }
}