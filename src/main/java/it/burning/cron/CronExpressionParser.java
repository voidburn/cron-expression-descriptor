package it.burning.cron;

import it.burning.utils.RxReplace;

import java.util.*;
import java.util.regex.Pattern;

import static it.burning.cron.CronExpressionParser.CronExpressionPart.*;

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
public class CronExpressionParser {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region FIELDS

    // Config
    private static final String LOCALIZATION_BUNDLE = "localization";
    private static final int    MIN_YEAR            = 1970;
    private static final int    MAX_YEAR            = 2099;
    private static final int    MIN_YEAR_FREQUENCY  = 0;
    private static final int    MAX_YEAR_FREQUENCY  = MAX_YEAR - MIN_YEAR;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PLEASE NOTE:
    //
    // Validation patterns are applied to normalized parts, not to the raw expression. Please refer to the
    // normalizeExpression() method for details on how the parts are transformed during that process. This affects the
    // expected patterns we allow for each part.

    // SECONDS and MINUTES in the range and frequencies 0-59
    //
    // ^(?:\\*|^0)$                                         -> Every step {0 or *}
    // ^(?:[0-5]?[0-9])$                                    -> Single value {0-59}
    // ^(?:(?:\\*|[0-5]?[0-9])/[0-5]?[0-9])$                -> Frequency range {* | 0-59}/{0-59} (expressions such as 0/2 are normalized to */2 so must be considered valid)
    // ^(?:([0-5]?[0-9],)*)(?:(?!^)[0-5]?[0-9])$            -> Multiple values {0-59},{0-59},{0-59}...
    // ^(?:[0-5]?[0-9])-(?:[0-5]?[0-9])$                    -> Range {0-59}-{0-59}
    // ^(?:[0-5]?[0-9])-(?:[0-5]?[0-9])/(?:[0-5]?[0-9])$    -> Range AND Frequency {0-59}-{0-59}/{0-59}
    private final Pattern secsAndMinsValidationPattern = Pattern.compile("^(?:\\*|^0)$|^(?:[0-5]?[0-9])$|^(?:(?:\\*|[0-5]?[0-9])/[0-5]?[0-9])$|^(?:([0-5]?[0-9],)*)(?:(?!^)[0-5]?[0-9])$|^(?:[0-5]?[0-9])-(?:[0-5]?[0-9])$|^(?:[0-5]?[0-9])-(?:[0-5]?[0-9])/(?:[0-5]?[0-9])$");

    // HOURS in the range and frequencies 0-23
    //
    // ^(?:\\*|^0)$                                                                                     -> Every step {0 or *}
    // ^(?:[0-1]?[0-9]|2?[0-3])$                                                                        -> Single value {0-23}
    // ^(?:(?:\\*|[0-1]?[0-9])|(?:2[0-3]))/(?:(?:[0-1]?[0-9])|(?:2[0-3]))$                              -> Frequency rage {* | 0-23}/{0-23} (expressions such as 0/2 are normalized to */2 so must be considered valid)
    // ^(?:(?:[0-1]?[0-9],)|(?:2[0-3],))*(?:(?:(?!^)[0-1]?[0-9])|(?:(?!^)2[0-3]))$                      -> Multiple values {0-23},{0-23},{0-23},...
    // ^(?:(?:[0-1]?[0-9])|(?:2[0-3]))-(?:(?:[0-1]?[0-9])|(?:2[0-3]))$                                  -> Range {0-23}-{0-23}
    // ^(?:(?:[0-1]?[0-9])|(?:2[0-3]))-(?:(?:[0-1]?[0-9])|(?:2[0-3]))/(?:(?:[0-1]?[0-9])|(?:2[0-3]))$   -> Range AND Frequency {0-23}-{0-23}/{0-23}
    private final Pattern hoursValidationPattern = Pattern.compile("^(?:\\*|^0)$|^(?:[0-1]?[0-9]|2?[0-3])$|^(?:(?:\\*|[0-1]?[0-9])|(?:2[0-3]))/(?:(?:[0-1]?[0-9])|(?:2[0-3]))$|^(?:(?:[0-1]?[0-9],)|(?:2[0-3],))*(?:(?:(?!^)[0-1]?[0-9])|(?:(?!^)2[0-3]))$|^(?:(?:[0-1]?[0-9])|(?:2[0-3]))-(?:(?:[0-1]?[0-9])|(?:2[0-3]))$|^(?:(?:[0-1]?[0-9])|(?:2[0-3]))-(?:(?:[0-1]?[0-9])|(?:2[0-3]))/(?:(?:[0-1]?[0-9])|(?:2[0-3]))$");

    // DAYS OF MONTH in the range and frequency 1-31
    //
    // ^(?:\\*)$                                                                                                            -> Every step {*}
    // ^(?:[1-9]|1[0-9]|2[0-9]|3[0-1])$                                                                                     -> Single value {1-31}
    // ^(?:\\*|[1-9]|1[0-9]|2[0-9]|3[0-1])/(?:[0-9]|1[0-9]|2[0-9]|3[0-1])$                                                  -> Frequency rage {* | 1-31}/{0-31} (expressions such as 1/31 are normalized to */31 so must be considered valid)
    // ^(?:(?:[1-9],)|(?:1[0-9],)|(?:2[0-9],)|(?:3[0-1],))+(?:(?:[1-9])|(?:1[0-9])|(?:2[0-9])|(?:3[0-1]))$                  -> Multiple values {1-31},{1-31},{1-31},...
    // ^(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])-(?:[1-9]|1[0-9]|2[0-9]|3[0-1]))$                                                  -> Range {1-31}-{1-31}
    // ^(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])-(?:[1-9]|1[0-9]|2[0-9]|3[0-1]))/(?:[0-9]|1[0-9]|2[0-9]|3[0-1])$                   -> Range AND Frequency {1-31}-{1-31}/{0-31}
    // ^(?:(?:L)|(?:LW)|(?:L)-(?:[1-9]|1[0-9]|2[0-9]|30)|(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])W))$                              -> Last day notations {
    //                                                                                                                            L (last day of the month),
    //                                                                                                                            LW (last day of the week),
    //                                                                                                                            L-{1-30} Nth day befor the end of the month,
    //                                                                                                                            {1-31}W On the nearest day to the Nth of the month
    //                                                                                                                         }
    private final Pattern domValidationPattern = Pattern.compile("^(?:\\*)$|^(?:[1-9]|1[0-9]|2[0-9]|3[0-1])$|^(?:\\*|[1-9]|1[0-9]|2[0-9]|3[0-1])/(?:[0-9]|1[0-9]|2[0-9]|3[0-1])$|^(?:(?:[1-9],)|(?:1[0-9],)|(?:2[0-9],)|(?:3[0-1],))+(?:(?:[1-9])|(?:1[0-9])|(?:2[0-9])|(?:3[0-1]))$|^(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])-(?:[1-9]|1[0-9]|2[0-9]|3[0-1]))$|^(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])-(?:[1-9]|1[0-9]|2[0-9]|3[0-1]))/(?:[0-9]|1[0-9]|2[0-9]|3[0-1])$|^(?:(?:L)|(?:LW)|(?:L)-(?:[1-9]|1[0-9]|2[0-9]|30)|(?:(?:[1-9]|1[0-9]|2[0-9]|3[0-1])W))$");

    // MONTHS in the range and frequencies 1-12
    //
    // ^(?:\*)$                                             -> Every step {*}
    // ^(?:[1-9]|1[0-2])$                                   -> Single value {1-12}
    // ^(?:\\*|[1-9]|1[0-2])/(?:[0-9]|1[0-2])$              -> Frequency range {* | 1-12}/{0-12} (expressions such as 1/12 are normalized to */12 so must be considered valid)
    // ^(?:[1-9],|1[0-2],)*(?:(?!^)[1-9]|(?!^)1[0-2])$      -> Multiple values {1-12},{1-12},{1-12}...
    // ^(?:[1-9]|1[0-2])-(?:[1-9]|1[0-2])$                  -> Range {1-12}-{1-12}
    // ^(?:[1-9]|1[0-2])-(?:[1-9]|1[0-2])/(?:[0-9]|1[0-2])$ -> Range AND Frequency {1-12}-{1-12}/{0-12}
    private final Pattern monthsValidationPattern = Pattern.compile("^(?:\\*)$|^(?:[1-9]|1[0-2])$|^(?:\\*|[1-9]|1[0-2])/(?:[0-9]|1[0-2])$|^(?:[1-9],|1[0-2],)*(?:(?!^)[1-9]|(?!^)1[0-2])$|^(?:[1-9]|1[0-2])-(?:[1-9]|1[0-2])$|^(?:[1-9]|1[0-2])-(?:[1-9]|1[0-2])/(?:[0-9]|1[0-2])$");

    // DAY OF WEEK in the range 0-6
    //
    // ^(?:\*)$                         -> Every step {*}
    // ^(?:[0-6])$                      -> Single value {0-6}
    // ^(?:\\*|[0-6])/(?:[0-6])$        -> Frequency range {* | 0-6}/{0-6} (expressions such as 1/7 are normalized to */7 so must be considered valid)
    // ^(?:[0-6],)*(?:(?!^)[0-6])$      -> Multiple values {0-6},{0-6},{0-6}...
    // ^(?:[0-6])-(?:[0-6])$            -> Range {0-6}-{0-6}
    // ^(?:[0-6])-(?:[0-6])/(?:[0-7])$  -> Range AND Frequency {0-6}-{0-6}/{0-7}
    // ^(?:[0-6]L)$                     -> Last weekday of the month {0-6}L
    // ^(?:[0-6]#[1-5])$                -> Nth Weekday of the month {0-6}#{1-5}
    private final Pattern dowValidationPattern = Pattern.compile("^(?:\\*)$|^(?:[0-6])$|^(?:\\*|[0-6])/(?:[0-6])$|^(?:[0-6],)*(?:(?!^)[0-6])$|^(?:[0-6])-(?:[0-6])$|^(?:[0-6])-(?:[0-6])/(?:[0-7])$|^(?:[0-6]L)$|^(?:[0-6]#[1-5])$");

    // YEARS in the range 1970-2999
    //
    // ^(?:\\*)$                                -> Every step {*}
    // ^\\d{4}$                                 -> Single value {any 4 digit number}
    // ^(?:\\*|\\d{4})/(?:\\d{1,3})$            -> Frequency range {* | any 4 digit number}/{any 3 digit number} (specific validity must be checked outside the match -> 1970-2099 / 1-129)
    // ^(?:\\d{4},)*(?:(?!^)\\d{4})$            -> Multiple values {any 4 digit number},{any 4 digit number},{any 4 digit number}... (specific validity must be checked outside the match -> 1970-2099)
    // ^(?:\\d{4})-(?:\\d{4})$                  -> Range {any 4 digit number}-{any 4 digit number} (specific validity must be checked outside the match -> 1970-2099)
    // ^(?:\\d{4})-(?:\\d{4})/(?:\\d{1,3})$     -> Range AND Frequency {any 4 digit number}-{any 4 digit number}/{any 3 digit number}
    private final Pattern yearsValidationPattern = Pattern.compile("^(?:\\*)$|^\\d{4}$|^(?:\\*|\\d{4})/(?:\\d{1,3})$|^(?:\\d{4},)*(?:(?!^)\\d{4})$|^(?:\\d{4})-(?:\\d{4})$|^(?:\\d{4})-(?:\\d{4})/(?:\\d{1,3})$");

    // Pattern matching
    private final Pattern   yearPattern             = Pattern.compile(".*\\d{4}$");
    private final Pattern   rangeTokenSearchPattern = Pattern.compile("[*/]");
    private final Pattern   stepValueSearchPattern  = Pattern.compile("[*\\-,]");
    private final Pattern   singleItemTokenPattern  = Pattern.compile("^[0-9]+$");
    private final RxReplace dowReplacer             = new RxReplace("(^\\d)|([^#/\\s]\\d)") {
        @Override
        public String replacement() {
            // Skip anything preceeded by # or /
            final String value = group(1) != null ? group(1) : group(2);

            // Extract digit part (i.e. if "-2" or ",2", just take 2)
            final String dowDigits = value.replaceAll("\\D", "");
            String dowDigitsAdjusted = dowDigits;

            // We're about to adjust based on a start index, we should reject out of bounds values before we do so
            if (Integer.parseInt(dowDigits) > 7) {
                throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldDoW")), DOW);
            }

            if (options.useJavaEeScheduleExpression) {
                if (partsCount == 5) {
                    if (dowDigits.equals("7")) {
                        dowDigitsAdjusted = "0";
                    }
                }
            } else {
                // Adjust Day of Week index for regular cron expressions (5 parts only). In regular cron "7" is accepted as sunday but not considered standard.
                if (partsCount == 5) {
                    if (dowDigits.equals("7")) {
                        dowDigitsAdjusted = "0";
                    }
                } else {
                    // If the expression has more than 5 parts (which means it includes seconds and/or years), Sunday is specified as 1 and Saturday is specified as 7.
                    // To normalize, we bring it back in the 0-6 range.
                    //
                    // See Quartz cron triggers (http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html)
                    dowDigitsAdjusted = String.valueOf(Integer.parseInt(dowDigits) - 1);
                }
            }


            return value.replace(dowDigits, dowDigitsAdjusted);
        }
    };

    // Data types
    public enum CronExpressionPart {
        SEC("SECOND"),
        MIN("MINUTE"),
        HOUR("HOUR"),
        DOM("DAY OF MONTH"),
        MONTH("MONTH"),
        DOW("DAY OF WEEK"),
        YEAR("YEAR"),
        ALL("EXPRESSION");

        private final String value;

        public String getValue() {
            return value;
        }

        CronExpressionPart(final String value) {
            this.value = value;
        }
    }

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
    private final String         expression;
    private final Options        options;
    private final ResourceBundle localization;
    private       int            partsCount;

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region SUBCLASSES

    // Parse exception
    public static class CronExpressionParseException extends RuntimeException {
        final CronExpressionPart part;

        public CronExpressionPart getPart() {
            return part;
        }

        public CronExpressionParseException(final String message, final CronExpressionPart part) {
            super(message);
            this.part = part;
        }
    }

    // Parse options
    public static class Options {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //region FIELDS

        // Defaults
        private boolean throwExceptionOnParseError  = true;
        private boolean verbose                     = false;
        private boolean use24HourTimeFormat         = true;
        private Locale  locale                      = Locale.getDefault();
        private boolean useJavaEeScheduleExpression = false;

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

        public boolean use24HourTimeFormat() {
            return use24HourTimeFormat;
        }

        public void setUse24HourTimeFormat(boolean use24HourTimeFormat) {
            this.use24HourTimeFormat = use24HourTimeFormat;
        }

        public void setUseJavaEeScheduleExpression(boolean useJavaEeScheduleExpression) {
            this.useJavaEeScheduleExpression = useJavaEeScheduleExpression;
        }

        public boolean getUseJavaEeScheduleExpression() {
            return this.useJavaEeScheduleExpression;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(String language) {
            setLocale(language != null ? new Locale(language) : Locale.getDefault());
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
    public CronExpressionParser(final String expression) {
        this(expression, null);
    }

    /**
     * Constructor
     *
     * @param expression The complete cron expression
     * @param options    Parsing options (null for defaults)
     */
    public CronExpressionParser(final String expression, final Options options) {
        this.expression = expression;
        this.options = options != null ? options : new Options();
        this.localization = ResourceBundle.getBundle(LOCALIZATION_BUNDLE, this.options.getLocale());
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
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize all elements of parsed array to empty strings
        final String[] parsed = new String[]{"", "", "", "", "", "", ""};
        final String[] tokenizedExpression = expression.split(" ");
        final List<String> tmp = new ArrayList<>();
        for (final String token : tokenizedExpression) {
            if (!token.isEmpty()) {
                tmp.add(token);
            }
        }

        // Determine how many significant parts the expression contains
        final String[] expressionParts = new String[tmp.size()];
        tmp.toArray(expressionParts);
        partsCount = expressionParts.length;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Inspect the expression parts
        if (partsCount < 5) {
            throw new CronExpressionParseException(String.format(getString("InvalidExpressionFormatTooFewParts"), expression, partsCount), ALL);
        } else if (partsCount == 5) {
            // 5 part cron so shift array past seconds element
            System.arraycopy(expressionParts, 0, parsed, 1, 5);
        } else if (partsCount == 6) {
            // We will detect if this 6 part expression has a year specified and if so we will shift the parts and treat the
            // first part as a minute part rather than a second part.
            //
            // Ways we detect:
            //   1. Last part is a literal year (i.e. 2020)
            //   2. 3rd or 5th part is specified as "?" (DOM or DOW)
            boolean isYearWithNoSecondsPart = yearPattern.matcher(expressionParts[5]).matches() || expressionParts[4].equals("?") || expressionParts[2].equals("?");
            if (isYearWithNoSecondsPart) {
                System.arraycopy(expressionParts, 0, parsed, 1, 6);
            } else {
                System.arraycopy(expressionParts, 0, parsed, 0, 6);
            }
        } else if (partsCount == 7) {
            // All parts are in use
            System.arraycopy(expressionParts, 0, parsed, 0, 7);
        } else {
            if (options.throwExceptionOnParseError) {
                throw new CronExpressionParseException(String.format(getString("InvalidExpressionFormatTooManyParts"), expression, partsCount), ALL);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Normalize the expression
        normalizeExpression(parsed);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Validate parts

        // Check if both DoM and DoW have been specified (? is normalized to * at this stage)
        if (partsCount > 5 && (!parsed[3].equals("*") && !parsed[5].equals("*"))) {
            throw new CronExpressionParseException(getString("InvalidDomDowExpression"), ALL);
        }

        // Check seconds
        if (partsCount > 5 && (!parsed[0].isEmpty() && !secsAndMinsValidationPattern.matcher(parsed[0]).matches())) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldSecond")), SEC);
        }

        // Check minutes
        if (parsed[1].isEmpty() || !secsAndMinsValidationPattern.matcher(parsed[1]).matches()) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldMinute")), MIN);
        }

        // Check hours
        if (parsed[2].isEmpty() || !hoursValidationPattern.matcher(parsed[2]).matches()) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldHour")), HOUR);
        }

        // Check Day of Month
        if (parsed[3].isEmpty() || !domValidationPattern.matcher(parsed[3]).matches()) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldDoM")), DOM);
        }

        // Check Month
        if (parsed[4].isEmpty() || !monthsValidationPattern.matcher(parsed[4]).matches()) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldMonth")), MONTH);
        }

        // Check Day of Week
        if (parsed[5].isEmpty() || !dowValidationPattern.matcher(parsed[5]).matches()) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldDoW")), DOW);
        }

        // Check year
        if (partsCount > 5 && (!parsed[6].isEmpty() && !yearsValidationPattern.matcher(parsed[6]).matches())) {
            throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldYear")), YEAR);
        } else if (!parsed[6].isEmpty() && yearsValidationPattern.matcher(parsed[6]).matches()) {
            if (partsCount > 5 && parsed[6].contains("/")) {
                final String[] frequencyParts = parsed[6].split("/");
                if (frequencyParts.length == 2) {
                    // Check range if present
                    if (frequencyParts[0].contains("-")) {
                        final String[] rangeParts = frequencyParts[0].split("-");
                        if (rangeParts.length == 2) {
                            // Check if range parts are out of bounds
                            if (Integer.parseInt(rangeParts[0]) < MIN_YEAR || Integer.parseInt(rangeParts[0]) > MAX_YEAR ||
                                Integer.parseInt(rangeParts[1]) < MIN_YEAR || Integer.parseInt(rangeParts[1]) > MAX_YEAR) {

                                throw new CronExpressionParseException(String.format(getString("InvalidYearsRangeValue"), MIN_YEAR, MAX_YEAR), YEAR);
                            }
                        }
                    } else {
                        // Frequency only, validate single year entry
                        if (!frequencyParts[0].equals("*") && (Integer.parseInt(frequencyParts[0]) < MIN_YEAR || Integer.parseInt(frequencyParts[0]) > MAX_YEAR)) {
                            throw new CronExpressionParseException(String.format(getString("InvalidYearsRangeValue"), MIN_YEAR, MAX_YEAR), YEAR);
                        }
                    }

                    // Validate frequency
                    if (Integer.parseInt(frequencyParts[1]) < MIN_YEAR_FREQUENCY || Integer.parseInt(frequencyParts[1]) > MAX_YEAR_FREQUENCY) {
                        throw new CronExpressionParseException(String.format(getString("InvalidYearFrequencyValue"), MIN_YEAR_FREQUENCY, MAX_YEAR_FREQUENCY), YEAR);
                    }
                } else {
                    throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), getString("InvalidFieldYear")), YEAR);
                }
            } else if (parsed[6].contains("-")) {
                // Check if range parts are out of bounds
                final String[] rangeParts = parsed[6].split("-");
                if (Integer.parseInt(rangeParts[0]) < MIN_YEAR || Integer.parseInt(rangeParts[0]) > MAX_YEAR ||
                    Integer.parseInt(rangeParts[1]) < MIN_YEAR || Integer.parseInt(rangeParts[1]) > MAX_YEAR) {

                    throw new CronExpressionParseException(String.format(getString("InvalidYearsRangeValue"), MIN_YEAR, MAX_YEAR), YEAR);
                }
            } else if (!parsed[6].equals("*")) {
                // Check single value
                if (Integer.parseInt(parsed[6]) < MIN_YEAR || Integer.parseInt(parsed[6]) > MAX_YEAR) {

                    throw new CronExpressionParseException(String.format(getString("InvalidYearsRangeValue"), MIN_YEAR, MAX_YEAR), YEAR);
                }
            }
        }

        return parsed;
    }

    /**
     * Massage the parsed expression into a format that can be digested by the ExpressionDescriptor
     *
     * @param parsed The parsed expression parts
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
                        stepRangeThrough = String.valueOf(MAX_YEAR);
                        break;
                    default:
                        break;
                }

                if (stepRangeThrough != null) {
                    final String[] steps = parsed[i].split("/");
                    if (steps.length > 2) {
                        final CronExpressionPart errorRange;
                        final String fieldString;
                        if (stepRangeThrough.equals("12")) {
                            errorRange = MONTH;
                            fieldString = getString("InvalidFieldMonth");
                        } else if (stepRangeThrough.equals("6")) {
                            errorRange = DOW;
                            fieldString = getString("InvalidFieldDoW");
                        } else {
                            errorRange = YEAR;
                            fieldString = getString("InvalidFieldYear");
                        }

                        throw new CronExpressionParseException(String.format(getString("InvalidFieldExpressionFormat"), fieldString), errorRange);
                    }

                    parsed[i] = String.format("%d-%d/%d", Integer.parseInt(steps[0]), Integer.parseInt(stepRangeThrough), Integer.parseInt(steps[1]));
                }
            }
        }
    }

    /**
     * Gets a localized String resource
     *
     * @param resourceName The name of the resource String to retrieve
     * @return The resource value
     */
    protected String getString(final String resourceName) {
        try {
            return localization.getString(resourceName);
        } catch (MissingResourceException e) {
            return "{{" + resourceName + "}}";
        }
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
