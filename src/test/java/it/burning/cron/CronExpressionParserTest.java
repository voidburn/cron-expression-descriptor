package it.burning.cron;

import it.burning.cron.CronExpressionParser.CronExpressionParseException;
import it.burning.cron.CronExpressionParser.CronExpressionPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CronExpressionParserTest {
    CronExpressionParseException exception;

    @Test
    void parse() {
        // Parts
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("5 */3 9").parse(), "A cron expression with less than 5 parts should throw an exception");
        assertEquals(CronExpressionPart.ALL, exception.getPart());
        assertEquals("The cron expression \"5 */3 9\" only has [3] parts. At least 5 parts are required.", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("5 */3 9 2 DEC,JAN,MAR MON,WED 2020, 2").parse(), "A cron expression with more than 7 parts should throw an exception");
        assertEquals(CronExpressionPart.ALL, exception.getPart());
        assertEquals("The cron expression \"5 */3 9 2 DEC,JAN,MAR MON,WED 2020, 2\" has too many parts [8]. Expressions must not have more than 7 parts.", exception.getMessage());

        // Day of Month and Day of Week
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("5 */3 9 2 DEC,JAN,MAR MON,WED 2020").parse(), "A cron expression cannot specify both DoM and DoW");
        assertEquals(CronExpressionPart.ALL, exception.getPart());
        assertEquals("Specifying both a Day of Month and Day of Week is not supported. Either one or the other should be declared as \"?\"", exception.getMessage());

        // Seconds
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("60 */3 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.SEC, exception.getPart());
        assertEquals("The expression describing the SECOND field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("6/60 */3 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.SEC, exception.getPart());
        assertEquals("The expression describing the SECOND field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("6-60 */3 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.SEC, exception.getPart());
        assertEquals("The expression describing the SECOND field is not in a valid format", exception.getMessage());

        // Minutes
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* 60 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.MIN, exception.getPart());
        assertEquals("The expression describing the MINUTE field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* 6/60 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.MIN, exception.getPart());
        assertEquals("The expression describing the MINUTE field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* 6-60 9 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.MIN, exception.getPart());
        assertEquals("The expression describing the MINUTE field is not in a valid format", exception.getMessage());

        // Hours
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * 24  ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.HOUR, exception.getPart());
        assertEquals("The expression describing the HOUR field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * 2/24 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.HOUR, exception.getPart());
        assertEquals("The expression describing the HOUR field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * 2-24 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.HOUR, exception.getPart());
        assertEquals("The expression describing the HOUR field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * 0-23/24 ? DEC,JAN,MAR MON,WED 2020").parse(), "");
        assertEquals(CronExpressionPart.HOUR, exception.getPart());
        assertEquals("The expression describing the HOUR field is not in a valid format", exception.getMessage());

        // DoM
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * 0 DEC,JAN,MAR ? 2020").parse(), "");
        assertEquals(CronExpressionPart.DOM, exception.getPart());
        assertEquals("The expression describing the DAY OF MONTH field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * 0/31 DEC,JAN,MAR ? 2020").parse(), "");
        assertEquals(CronExpressionPart.DOM, exception.getPart());
        assertEquals("The expression describing the DAY OF MONTH field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * 0-31 DEC,JAN,MAR ? 2020").parse(), "");
        assertEquals(CronExpressionPart.DOM, exception.getPart());
        assertEquals("The expression describing the DAY OF MONTH field is not in a valid format", exception.getMessage());

        // Month
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? JAN/OCT/2 * 2020").parse(), "");
        assertEquals(CronExpressionPart.MONTH, exception.getPart());
        assertEquals("The expression describing the MONTH field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? 1-13 ? 2020").parse(), "");
        assertEquals(CronExpressionPart.MONTH, exception.getPart());
        assertEquals("The expression describing the MONTH field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? 13/0 ? 2020").parse(), "");
        assertEquals(CronExpressionPart.MONTH, exception.getPart());
        assertEquals("The expression describing the MONTH field is not in a valid format", exception.getMessage());

        // DoW
        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? * SUN/MON/TUE 2020").parse(), "");
        assertEquals(CronExpressionPart.DOW, exception.getPart());
        assertEquals("The expression describing the DAY OF WEEK field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? * 1/8 2020").parse(), "");
        assertEquals(CronExpressionPart.DOW, exception.getPart());
        assertEquals("The expression describing the DAY OF WEEK field is not in a valid format", exception.getMessage());

        exception = assertThrows(CronExpressionParseException.class, () -> new CronExpressionParser("* * * ? * 8/0 2020").parse(), "");
        assertEquals(CronExpressionPart.DOW, exception.getPart());
        assertEquals("The expression describing the DAY OF WEEK field is not in a valid format", exception.getMessage());

        // Year

        // Test valid parse
        final String[] parsed = new CronExpressionParser("5 0/3 9 2 DEC,JAN,MAR ? 2020").parse();
        assertEquals("5", parsed[0], "Second should be 5");
        assertEquals("*/3", parsed[1], "Minute should be */3");
        assertEquals("9-9", parsed[2], "Hour should be 9-9");
        assertEquals("2", parsed[3], "DoW should be 2");
        assertEquals("12,1,3", parsed[4], "Month should be 12,1,3");
        assertEquals("*", parsed[5], "DoM should be 1,3");
        assertEquals("2020", parsed[6], "Year should be 2020");
    }
}