package it.burning.cron;

import it.burning.cron.CronExpressionParser.CronExpressionParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CronExpressionParserTest {
    final CronExpressionParser validParser = new CronExpressionParser("5 */3 9 2 DEC,JAN,MAR ? 2020");
    final CronExpressionParser parserShort = new CronExpressionParser("5 */3 9");
    final CronExpressionParser parserLong  = new CronExpressionParser("5 */3 9 2 DEC,JAN,MAR MON,WED 2020, 2");

    @Test
    void parse() {
        // Test parse exceptions
        final RuntimeException shortException = assertThrows(CronExpressionParseException.class, parserShort::parse, "A cron expression with less than 5 parts should throw an exception");
        assertEquals("The cron expression \"5 */3 9\" only has [3] parts. At least 5 parts are required.", shortException.getMessage());

        final RuntimeException longException = assertThrows(CronExpressionParseException.class, parserLong::parse, "A cron expression with more than 7 parts should throw an exception");
        assertEquals("The cron expression \"5 */3 9 2 DEC,JAN,MAR MON,WED 2020, 2\" has too many parts [8]. Expressions must not have more than 7 parts.", longException.getMessage());

        // Test valid parse
        final String[] parsed = validParser.parse();
        assertEquals("5", parsed[0], "Second should be 5");
        assertEquals("*/3", parsed[1], "Minute should be */3");
        assertEquals("9-9", parsed[2], "Hour should be 9-9");
        assertEquals("2", parsed[3], "DoW should be 2");
        assertEquals("12,1,3", parsed[4], "Month should be 12,1,3");
        assertEquals("*", parsed[5], "DoM should be 1,3");
        assertEquals("2020", parsed[6], "Year should be 2020");
    }
}