package it.burning.cron;

import it.burning.utils.RxReplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/*
Cron reference (Quartz)

┌───────────── second (0 - 59)
| ┌───────────── minute (0 - 59)
│ | ┌───────────── hour (0 - 23)
│ │ │ ┌───────────── day of month (1 - 31)
│ │ │ │ ┌───────────── month (1 - 12)
│ │ │ │ │ ┌───────────── day of week (0 - 6) (Sunday to Saturday; 7 is also Sunday on some systems)
│ │ │ │ │ │ ┌───────────── year (Optional)
│ │ │ │ │ │ │
│ │ │ │ │ │ │
| │ │ │ │ │ │
* * * ? * * * command to execute

*/
public class ExpressionParser {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region FIELDS

    // Pattern matching
    private final Pattern   yearPattern             = Pattern.compile(".*\\d{4}$");
    private final Pattern   rangeTokenSearchPattern = Pattern.compile("[*/]");
    private final Pattern   stepValueSearchPattern  = Pattern.compile("[*\\-,]");
    private final Pattern   singleItemTokenPattern  = Pattern.compile("^[0-9]+$");
    private final RxReplace dowReplacer             = new RxReplace("(^\\d)|([^#/\\s]\\d)") {
        @Override
        public String replacement() {
            // Skip anything preceeding by # or /
            final String value = group(1) != null ? group(1) : group(2);

            // Extract digit part (i.e. if "-2" or ",2", just take 2)
            final String dowDigits = value.replaceAll("\\D", "");
            String dowDigitsAdjusted = dowDigits;

            if (options.isDayOfWeekStartIndexZero()) {
                // "7" also means Sunday so we will convert to "0" to normalize it
                if (dowDigits.equals("7")) {
                    dowDigitsAdjusted = "0";
                }
            } else {
                // If dayOfWeekStartIndexZero==false, Sunday is specified as 1 and Saturday is specified as 7.
                // To normalize, we will shift the DOW number down so that 1 becomes 0, 2 becomes 1, and so on.
                dowDigitsAdjusted = String.valueOf(Integer.parseInt(dowDigits) - 1);
            }

            return value.replace(dowDigits, dowDigitsAdjusted);
        }
    };

    // Data
    public enum Day {
        SUN,
        MON,
        TUE,
        WED,
        THU,
        FRI,
        SAT
    }

    public enum Month {
        JAN,
        FEB,
        MAR,
        APR,
        MAY,
        JUN,
        JUL,
        AUG,
        SEP,
        OCT,
        NOV,
        DEC
    }

    // State
    private final String  expression;
    private final Options options;

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region SUBCLASSES

    public static class Options {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //region FIELDS

        // Defaults
        private boolean throwExceptionOnParseError = true;
        private boolean verbose                    = true;
        private boolean dayOfWeekStartIndexZero    = false;
        private boolean use24HourTimeFormat        = true;
        private Locale  locale                     = Locale.getDefault();

        //endregion
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //region ACCESSORS

        public boolean isThrowExceptionOnParseError() {
            return throwExceptionOnParseError;
        }

        public void setThrowExceptionOnParseError(boolean throwExceptionOnParseError) {
            this.throwExceptionOnParseError = throwExceptionOnParseError;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public boolean isDayOfWeekStartIndexZero() {
            return dayOfWeekStartIndexZero;
        }

        public void setDayOfWeekStartIndexZero(boolean dayOfWeekStartIndexZero) {
            this.dayOfWeekStartIndexZero = dayOfWeekStartIndexZero;
        }

        public boolean use24HourTimeFormat() {
            return use24HourTimeFormat;
        }

        public void setUse24HourTimeFormat(boolean use24HourTimeFormat) {
            this.use24HourTimeFormat = use24HourTimeFormat;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(String language) {
            setLocale((language != null && language.length() == 2) ? new Locale(language) : Locale.getDefault());
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        //endregion
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //region CONSTRUCTORS

        /**
         * Constructor
         */
        public Options() {

        }

        //endregion
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region CONSTRUCTORS

    /**
     * Constructor (Init with default options)
     *
     * @param expression The complete cron expression
     */
    public ExpressionParser(final String expression) {
        this(expression, null);
    }

    /**
     * Constructor
     *
     * @param expression The complete cron expression
     * @param options    Parsing options (null for defaults)
     */
    public ExpressionParser(final String expression, final Options options) {
        this.expression = expression;
        this.options = options != null ? options : new Options();
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region METHODS

    /**
     * Parses the cron expression string
     *
     * @return A 7 part string array, one part for each component of the cron expression (seconds, minutes, etc.)
     */
    public String[] parse() {
        // Initialize all elements of parsed array to empty strings
        final String[] parsed = new String[]{"", "", "", "", "", "", ""};

        if (expression == null || expression.isEmpty()) {
            throw new RuntimeException("Field 'expression' not found.");
        } else {
            // Tokenize expression and discard empty entries
            final String[] tokenizedExpression = expression.split(" ");
            final List<String> tmp = new ArrayList<>();
            for (final String token : tokenizedExpression) {
                if (!token.isEmpty()) {
                    tmp.add(token);
                }
            }
            final String[] expressionParts = new String[tmp.size()];
            tmp.toArray(expressionParts);

            // Inspect the expression parts
            if (expressionParts.length < 5) {
                throw new RuntimeException(String.format("Error: Expression only has %d parts.  At least 5 parts are required.", expressionParts.length));
            } else if (expressionParts.length == 5) {
                // 5 part cron so shift array past seconds element
                System.arraycopy(expressionParts, 0, parsed, 1, 5);
            } else if (expressionParts.length == 6) {
                // If the last element is a "year" definition assume that the missing part is "second" (first token)
                if (yearPattern.matcher(expressionParts[5]).matches()) {
                    System.arraycopy(expressionParts, 0, parsed, 1, 6);
                } else {
                    System.arraycopy(expressionParts, 0, parsed, 0, 6);
                }
            } else if (expressionParts.length == 7) {
                // All parts are in use
                System.arraycopy(expressionParts, 0, parsed, 0, 7);
            } else {
                throw new RuntimeException(String.format("Error: Expression has too many parts (%d).  Expression must not have more than 7 parts.", expressionParts.length));
            }
        }

        // Normalize the expression
        normalizeExpression(parsed);

        return parsed;
    }

    /**
     * Massage the parsed expression into a format that can be digested by the ExpressionDescriptor
     *
     * @param parsed
     */
    private void normalizeExpression(final String[] parsed) {
        // Convert ? to * only for DOM and DOW
        parsed[3] = parsed[3].replace("?", "*");
        parsed[5] = parsed[5].replace("?", "*");

        // Convert 0/, 1/ to */
        if (parsed[0].startsWith("0/")) {
            // Seconds
            parsed[0] = parsed[0].replace("0/", "*/");
        }

        if (parsed[1].startsWith("0/")) {
            // Minutes
            parsed[1] = parsed[1].replace("0/", "*/");
        }

        if (parsed[2].startsWith("0/")) {
            // Hours
            parsed[2] = parsed[2].replace("0/", "*/");
        }

        if (parsed[3].startsWith("1/")) {
            // DOM
            parsed[3] = parsed[3].replace("1/", "*/");
        }

        if (parsed[4].startsWith("1/")) {
            // Month
            parsed[4] = parsed[4].replace("1/", "*/");
        }

        if (parsed[5].startsWith("1/")) {
            // DOW
            parsed[5] = parsed[5].replace("1/", "*/");
        }

        if (parsed[6].startsWith("1/")) {
            // Years
            parsed[6] = parsed[6].replace("1/", "*/");
        }

        // Adjust DOW based on dayOfWeekStartIndexZero option
        parsed[5] = dowReplacer.replace(parsed[5]);

        // Convert DOM '?' to '*'
        if (parsed[3].equals("?")) {
            parsed[3] = "*";
        }

        // Convert SUN-SAT format to 0-6 format
        for (int i = 0; i <= 6; i++) {
            final String currentDay = Day.values()[i].name();
            parsed[5] = parsed[5].replace(currentDay, String.valueOf(i));

            // Found, early exit
            if (parsed[5].length() == 1) {
                break;
            }
        }

        // Convert JAN-DEC format to 1-12 format
        for (int i = 0; i < 12; i++) {
            final String currentMonth = Month.values()[i].name();
            parsed[4] = parsed[4].replace(currentMonth, String.valueOf(i + 1));

            // Found, early exit
            if (parsed[4].length() == 1 || parsed[4].length() == 2) {
                break;
            }
        }

        // Convert 0 second to (empty)
        if (parsed[0].equals("0")) {
            parsed[0] = "";
        }

        // If time interval is specified for seconds or minutes and next time part is single item, make it a "self-range" so
        // the expression can be interpreted as an interval 'between' range.
        //     For example:
        //     0-20/3 9 * * * => 0-20/3 9-9 * * * (9 => 9-9)
        //     */5 3 * * * => */5 3-3 * * * (3 => 3-3)
        if (singleItemTokenPattern.matcher(parsed[2]).matches() && (rangeTokenSearchPattern.matcher(parsed[1]).find() || rangeTokenSearchPattern.matcher(parsed[0]).find())) {
            parsed[2] += "-" + parsed[2];
        }

        // Loop through all parts and apply global normalization
        for (int i = 0; i < parsed.length; i++) {
            // Convert all '*/1' to '*'
            if (parsed[i].equals("*/1")) {
                parsed[i] = "*";
            }

            // Convert non specified ranges to "/N" -> "*/N"
            final String[] parts = parsed[i].split("/");
            if (parts.length > 1 && parts[0].isEmpty()) {
                parsed[i] = "*" + "/" + parts[1];
            }

            // Convert Month,DOW,Year step values with a starting value (i.e. not '*') to between expressions.
            // This allows us to reuse the between expression handling for step values.
            //
            // For Example:
            //  - month part '3/2' will be converted to '3-12/2' (every 2 months between March and December)
            //  - DOW part '3/2' will be converted to '3-6/2' (every 2 days between Tuesday and Saturday)
            if (parsed[i].contains("/") && !stepValueSearchPattern.matcher(parsed[i]).find()) {
                String stepRangeThrough = null;
                switch (i) {
                    case 4:
                        stepRangeThrough = "12";
                        break;
                    case 5:
                        stepRangeThrough = "6";
                        break;
                    case 6:
                        stepRangeThrough = "9999";
                        break;
                    default:
                        break;
                }

                if (stepRangeThrough != null) {
                    final String[] steps = parsed[i].split("/");
                    parsed[i] = String.format("%d-%d/%d", Integer.parseInt(steps[0]), Integer.parseInt(stepRangeThrough), Integer.parseInt(steps[1]));
                }
            }
        }
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
