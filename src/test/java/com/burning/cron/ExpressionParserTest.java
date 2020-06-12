package com.burning.cron;

import com.burning.cron.ExpressionParser.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionParserTest {
    final ExpressionParser parser = new ExpressionParser("5 */3 9 2 DEC,JAN,MAR MON,WED 2020", new Options());

    @Test
    void parse() {
        final String[] parsed = parser.parse();

        System.out.println("Parsed expression:");
        for (int i = 0; i < parsed.length; i++) {
            final String part = parsed[i];

            if (part.isEmpty()) {
                System.out.println("\t[" + i +"] empty");
            } else {
                System.out.println("\t[" + i +"] " + part);
            }
        }

        assertEquals("5", parsed[0], "Second should be 5");
        assertEquals("*/3", parsed[1], "Minute should be 0-20/3");
        assertEquals("9-9", parsed[2], "Hour should be 9-9");
        assertEquals("2", parsed[3], "DoW should be 2");
        assertEquals("12,1,3", parsed[4], "Month should be 12,1,3");
        assertEquals("1,3", parsed[5], "DoM should be 1,3");
        assertEquals("2020", parsed[6], "Year should be 2020");
    }
}