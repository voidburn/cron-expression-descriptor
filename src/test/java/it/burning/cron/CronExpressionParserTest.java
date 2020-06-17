package it.burning.cron;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CronExpressionParserTest {
    final CronExpressionParser parser = new CronExpressionParser("5 */3 9 2 DEC,JAN,MAR MON,WED 2020");

    @Test
    void parse() {
        final String[] parsed = parser.parse();
        assertEquals("5", parsed[0], "Second should be 5");
        assertEquals("*/3", parsed[1], "Minute should be 0-20/3");
        assertEquals("9-9", parsed[2], "Hour should be 9-9");
        assertEquals("2", parsed[3], "DoW should be 2");
        assertEquals("12,1,3", parsed[4], "Month should be 12,1,3");
        assertEquals("1,3", parsed[5], "DoM should be 1,3");
        assertEquals("2020", parsed[6], "Year should be 2020");
    }
}