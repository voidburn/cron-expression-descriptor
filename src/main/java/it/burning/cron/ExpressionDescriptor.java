package it.burning.cron;

import it.burning.cron.ExpressionParser.Day;
import it.burning.cron.ExpressionParser.Month;
import it.burning.cron.ExpressionParser.Options;
import it.burning.utils.RxReplace;

import java.util.Calendar;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.burning.cron.ExpressionDescriptor.DescriptionType.FULL;

public class ExpressionDescriptor {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region FIELDS

    // Functional implementations
    @FunctionalInterface
    private interface GetDescription {
        String getFor(String description);
    }

    // Constants
    private static final String EMPTY_STRING        = "";
    private static final String LOCALIZATION_BUNDLE = "localization";

    // Patterns
    private final Pattern   specialCharactersSearchPattern       = Pattern.compile("[/\\-,*]");
    private final Pattern   lastDayOffsetPattern                 = Pattern.compile("L-(\\d{1,2})");
    private final Pattern   weekDayNumberMatches                 = Pattern.compile("(\\d{1,2}W)|(W\\d{1,2})");
    private final Pattern   yearPattern                          = Pattern.compile("(\\d{4})");
    private final Pattern   segmentRangesOrMultipleSearchPattern = Pattern.compile("[/\\-,]");
    private final Pattern   segmentAnyOrMultipleSearchPattern    = Pattern.compile("[*,]");
    private final RxReplace stripDescription                     = new RxReplace("[\\\\,\\ ?$]") {
        @Override
        public String replacement() {
            // Strip all matches
            return "";
        }
    };

    // Data
    public enum DescriptionType {
        FULL,
        TIMEOFDAY,
        SECONDS,
        MINUTES,
        HOURS,
        DAYOFWEEK,
        MONTH,
        DAYOFMONTH,
        YEAR
    }

    // State
    private final        String         expression;
    private              String[]       expressionParts;
    private final        Locale         locale;
    private final        ResourceBundle localization;
    private              boolean        parsed;
    private final        Options        options;
    private final        boolean        use24HourTimeFormat;
    private static final Options        defaultOptions = new Options();

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region ACCESSORS

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region CONSTRUCTORS

    /**
     * Constructor (default system locale)
     */
    public ExpressionDescriptor(final String expression) {
        this(expression, new Options());
    }

    /**
     * Constructor
     *
     * @param expression The cron expression to describe
     * @param options    The options to use when parsing the expression
     */
    public ExpressionDescriptor(final String expression, final Options options) {
        this.expression = expression;
        this.locale = options.getLocale();
        this.localization = ResourceBundle.getBundle(LOCALIZATION_BUNDLE, this.locale);
        this.parsed = false;
        this.options = options;
        this.use24HourTimeFormat = options.use24HourTimeFormat();
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region METHODS

    /**
     * Generates a human readable String for the Cron Expression
     *
     * @param type Which part(s) of the expression to describe
     * @return The cron expression description
     */
    public String getDescription(final DescriptionType type) {
        String description;
        try {
            if (!parsed) {
                final ExpressionParser parser = new ExpressionParser(expression, options);
                expressionParts = parser.parse();
                parsed = true;
            }

            switch (type) {
                case TIMEOFDAY:
                    description = GetTimeOfDayDescription();
                    break;
                case HOURS:
                    description = GetHoursDescription();
                    break;
                case MINUTES:
                    description = GetMinutesDescription();
                    break;
                case SECONDS:
                    description = GetSecondsDescription();
                    break;
                case DAYOFMONTH:
                    description = GetDayOfMonthDescription();
                    break;
                case MONTH:
                    description = GetMonthDescription();
                    break;
                case DAYOFWEEK:
                    description = GetDayOfWeekDescription();
                    break;
                case YEAR:
                    description = GetYearDescription();
                    break;
                default:
                    description = getFullDescription();
                    break;
            }
        } catch (final Exception e) {
            if (!options.isThrowExceptionOnParseError()) {
                description = e.getMessage();
            } else {
                throw e;
            }
        }

        // Uppercase the first letter
        description = description.substring(0, 1).toUpperCase() + description.substring(1);

        return description;
    }

    /**
     * Generates the FULL description
     *
     * @return
     */
    protected String getFullDescription() {
        String description;
        try {
            final String timeSegment = GetTimeOfDayDescription();
            final String dayOfMonthDesc = GetDayOfMonthDescription();
            final String monthDesc = GetMonthDescription();
            final String dayOfWeekDesc = GetDayOfWeekDescription();
            final String yearDesc = GetYearDescription();

            description = String.format("%s%s%s%s%s", timeSegment, dayOfMonthDesc, dayOfWeekDesc, monthDesc, yearDesc);
            description = transformVerbosity(description, options.isVerbose());
        } catch (final Exception e) {
            description = getString("AnErrorOccuredWhenGeneratingTheExpressionD");
            if (options.isThrowExceptionOnParseError()) {
                throw new RuntimeException(description, e);
            }
        }

        return description;
    }

    /**
     * Generates a description for only the TIMEOFDAY portion of the expression
     *
     * @return The TIMEOFDAY description
     */
    protected String GetTimeOfDayDescription() {
        final String secondsExpression = expressionParts[0];
        final String minuteExpression = expressionParts[1];
        final String hourExpression = expressionParts[2];
        final StringBuilder description = new StringBuilder();

        // Handle special cases first
        if (!specialCharactersSearchPattern.matcher(minuteExpression).find() && !specialCharactersSearchPattern.matcher(hourExpression).find() && !specialCharactersSearchPattern.matcher(secondsExpression).find()) {
            // Specific time of day (i.e. 10 14)
            description.append(getString("AtSpace")).append(formatTime(hourExpression, minuteExpression, secondsExpression));
        } else if (secondsExpression.equals("") && minuteExpression.contains("-") && !minuteExpression.contains(",") && !specialCharactersSearchPattern.matcher(hourExpression).find()) {
            // Minute range in single hour (i.e. 0-10 11)
            final String[] minuteParts = minuteExpression.split("-");
            description.append(String.format(getString("EveryMinuteBetweenX0AndX1"), formatTime(hourExpression, minuteParts[0]), formatTime(hourExpression, minuteParts[1])));
        } else if (secondsExpression.equals("") && hourExpression.contains(",") && !hourExpression.contains("-") && !specialCharactersSearchPattern.matcher(minuteExpression).find()) {
            // Hours list with single minute (o.e. 30 6,14,16)
            final String[] hourParts = hourExpression.split(",");
            description.append(getString("At"));
            for (int i = 0; i < hourParts.length; i++) {
                description.append(" ").append(formatTime(hourParts[i], minuteExpression));

                if (i < (hourParts.length - 2)) {
                    description.append(",");
                }

                if (i == hourParts.length - 2) {
                    description.append(getString("SpaceAnd"));
                }
            }
        } else {
            // Default time description
            final String secondsDescription = GetSecondsDescription();
            final String minutesDescription = GetMinutesDescription();
            final String hoursDescription = GetHoursDescription();

            description.append(secondsDescription);

            if (description.length() > 0 && minutesDescription.length() > 0) {
                description.append(", ");
            }

            description.append(minutesDescription);

            if (description.length() > 0 && hourExpression.length() > 0) {
                description.append(", ");
            }

            description.append(hoursDescription);
        }

        return description.toString();
    }

    /**
     * Generates a description for only the SECONDS portion of the expression
     *
     * @return
     */
    protected String GetSecondsDescription() {
        return getSegmentDescription(expressionParts[0],
                                     getString("EverySecond"),
                                     desc -> desc,
                                     desc -> String.format(getString("EveryX0Seconds"), desc),
                                     desc -> getString("SecondsX0ThroughX1PastTheMinute"),
                                     desc -> {
                                         try {
                                             final int i = Integer.parseInt(desc);

                                             if (desc.equals("0")) {
                                                 return "";
                                             } else if (i < 20) {
                                                 return getString("AtX0SecondsPastTheMinute");
                                             } else {
                                                 final String specialized = getString("AtX0SecondsPastTheMinuteGt20");
                                                 if (specialized != null) {
                                                     return specialized;
                                                 } else {
                                                     return getString("AtX0SecondsPastTheMinute");
                                                 }
                                             }
                                         } catch (NumberFormatException e) {
                                             // Parse failure, original implementation returs the default string anyway
                                             return getString("AtX0SecondsPastTheMinute");
                                         }
                                     },
                                     desc -> {
                                         final String specialized = getString("ComaMinX0ThroughMinX1");
                                         if (specialized != null) {
                                             return specialized;
                                         } else {
                                             return getString("ComaX0ThroughX1");
                                         }
                                     });
    }

    /**
     * Generates a description for only the MINUTE portion of the expression
     *
     * @return The MINUTE description
     */
    protected String GetMinutesDescription() {
        final String secondsExpression = expressionParts[0];

        return getSegmentDescription(expressionParts[1],
                                     getString("EveryMinute"),
                                     desc -> desc,
                                     desc -> String.format(getString("EveryX0Minutes"), desc),
                                     desc -> getString("MinutesX0ThroughX1PastTheHour"),
                                     desc -> {
                                         try {
                                             int target = Integer.parseInt(desc);
                                             if (desc.equals("0") && secondsExpression.equals("")) {
                                                 return "";
                                             } else if (target < 20) {
                                                 return getString("AtX0MinutesPastTheHour");
                                             } else {
                                                 final String specialFormat = getString("AtX0MinutesPastTheHourGt20");
                                                 if (specialFormat != null && !specialFormat.isEmpty()) {
                                                     return specialFormat;
                                                 } else {
                                                     return getString("AtX0MinutesPastTheHour");
                                                 }
                                             }
                                         } catch (NumberFormatException e) {
                                             return getString("AtX0MinutesPastTheHour");
                                         }
                                     },
                                     desc -> {
                                         final String specialFormat = getString("ComaMinX0ThroughMinX1");
                                         if (specialFormat != null && !specialFormat.isEmpty()) {
                                             return specialFormat;
                                         }

                                         return getString("ComaX0ThroughX1");
                                     });
    }

    /**
     * Generates a description for only the HOUR portion of the expression
     *
     * @return The HOUR description
     */
    protected String GetHoursDescription() {
        final String expression = expressionParts[2];

        return getSegmentDescription(expression, getString("EveryHour"), desc -> formatTime(desc, "0"), desc -> String.format(getString("EveryX0Hours"), desc), desc -> getString("BetweenX0AndX1"), desc -> getString("AtX0"), desc -> {
            final String specialFormat = getString("ComaMinX0ThroughMinX1");
            if (specialFormat != null && !specialFormat.isEmpty()) {
                return specialFormat;
            }

            return getString("ComaX0ThroughX1");
        });
    }

    /**
     * Generates a description for only the DAYOFWEEK portion of the expression
     *
     * @return The DAYOFWEEK description
     */
    protected String GetDayOfWeekDescription() {
        String description;
        if (expressionParts[5].equals("*")) {
            // DOW is specified as * so we will not generate a description and defer to DOM part.
            // Otherwise, we could get a contradiction like "on day 1 of the month, every day"
            // or a dupe description like "every day, every day".
            description = "";
        } else {
            description = getSegmentDescription(expressionParts[5],
                                                getString("ComaEveryDay"),
                                                desc -> {
                                                    // Drop "Last" identifier (L) if specified
                                                    if (desc.contains("L")) {
                                                        desc = desc.replace("L", "");
                                                    }

                                                    // Drop "day occurrence" identifier (#) if specified. Only retain the week-day's number.
                                                    if (desc.contains("#")) {
                                                        desc = desc.substring(0, desc.indexOf("#"));
                                                    }

                                                    final int dayNum = Integer.parseInt(desc);

                                                    return getString(Day.values()[dayNum].name());
                                                },
                                                desc -> String.format(getString("ComaEveryX0DaysOfTheWeek"), desc),
                                                desc -> getString("ComaX0ThroughX1"),
                                                desc -> {
                                                    String format;
                                                    if (desc.contains("#")) {
                                                        final String dayOfWeekOfMonthNumber = desc.substring(desc.indexOf("#") + 1);
                                                        String dayOfWeekOfMonthDescription = null;
                                                        switch (dayOfWeekOfMonthNumber) {
                                                            case "1":
                                                                dayOfWeekOfMonthDescription = getString("First");
                                                                break;
                                                            case "2":
                                                                dayOfWeekOfMonthDescription = getString("Second");
                                                                break;
                                                            case "3":
                                                                dayOfWeekOfMonthDescription = getString("Third");
                                                                break;
                                                            case "4":
                                                                dayOfWeekOfMonthDescription = getString("Fourth");
                                                                break;
                                                            case "5":
                                                                dayOfWeekOfMonthDescription = getString("Fifth");
                                                                break;
                                                        }


                                                        format = getString("ComaOnTheSpace") + dayOfWeekOfMonthDescription + getString("SpaceX0OfTheMonth");
                                                    } else if (desc.contains("L")) {
                                                        format = getString("ComaOnTheLastX0OfTheMonth");
                                                    } else {
                                                        format = getString("ComaOnlyOnX0");
                                                    }

                                                    return format;
                                                },
                                                desc -> getString("ComaX0ThroughX1"));
        }

        return description;
    }

    /**
     * Generates a description for only the MONTH portion of the expression
     *
     * @return
     */
    protected String GetMonthDescription() {
        return getSegmentDescription(expressionParts[4],
                                     "",
                                     desc -> {
                                         final int month = Integer.parseInt(desc) - 1; // Offset to match the enum's ordinals

                                         return getString(Month.values()[month].name());
                                     }, desc -> String.format(getString("ComaEveryX0Months"), desc),
                                     desc -> {
                                         final String specialFormat = getString("ComaMonthX0ThroughMonthX1");
                                         if (specialFormat != null && !specialFormat.isEmpty()) {
                                             return specialFormat;
                                         }

                                         return getString("ComaX0ThroughX1");
                                     }, desc -> getString("ComaOnlyInX0"),
                                     desc -> {
                                         final String specialFormat = getString("ComaMonthX0ThroughMonthX1");
                                         if (specialFormat != null && !specialFormat.isEmpty()) {
                                             return specialFormat;
                                         }

                                         return getString("ComaX0ThroughX1");
                                     });
    }

    /**
     * Generates a description for only the DAYOFMONTH portion of the expression
     *
     * @return The DAYOFMONTH description
     */
    protected String GetDayOfMonthDescription() {
        String description;
        final String expression = expressionParts[3];
        switch (expression) {
            case "L":
                description = getString("ComaOnTheLastDayOfTheMonth");
                break;
            case "WL":
            case "LW":
                description = getString("ComaOnTheLastWeekdayOfTheMonth");
                break;
            default:
                final Matcher weekDayNumberMatcher = weekDayNumberMatches.matcher(expression);
                if (weekDayNumberMatcher.matches()) {
                    final int weekDayNumber = Integer.parseInt(weekDayNumberMatcher.group(0).replace("W", ""));
                    final String dayString = weekDayNumber == 1 ? getString("FirstWeekday") : String.format(getString("WeekdayNearestDayX0"), weekDayNumber);

                    description = String.format(getString("ComaOnTheX0OfTheMonth"), dayString);
                } else {
                    // Handle "last day offset" (i.e. L-5:  "5 days before the last day of the month")
                    final Matcher lastDayOffsetMatcher = lastDayOffsetPattern.matcher(expression);
                    if (lastDayOffsetMatcher.matches()) {
                        final String offSetDays = lastDayOffsetMatcher.group(1);
                        description = String.format(getString("CommaDaysBeforeTheLastDayOfTheMonth"), offSetDays);
                    } else {
                        description = getSegmentDescription(expression,
                                                            getString("ComaEveryDay"),
                                                            desc -> desc,
                                                            desc -> {
                                                                if (desc.equals("1")) {
                                                                    return getString("ComaEveryDay");
                                                                }

                                                                return getString("ComaEveryX0Days");
                                                            },
                                                            desc -> getString("ComaBetweenDayX0AndX1OfTheMonth"),
                                                            desc -> getString("ComaOnDayX0OfTheMonth"),
                                                            desc -> getString("ComaX0ThroughX1"));

                    }
                }
                break;
        }

        return description;
    }

    /**
     * Generates a description for only the YEAR portion of the expression
     *
     * @return The YEAR description
     */
    private String GetYearDescription() {
        return getSegmentDescription(expressionParts[6],
                                     "",
                                     desc -> {
                                         if (yearPattern.matcher(desc).matches()) {
                                             final Calendar calendar = Calendar.getInstance(locale);
                                             calendar.set(Integer.parseInt(desc), Calendar.JANUARY, 1);

                                             return String.valueOf(calendar.get(Calendar.YEAR));
                                         }

                                         return desc;
                                     },
                                     desc -> String.format(getString("ComaEveryX0Years"), desc),
                                     desc -> {
                                         final String specialFormat = getString("ComaYearX0ThroughYearX1");
                                         if (specialFormat == null || !specialFormat.isEmpty()) {
                                             return specialFormat;
                                         }

                                         return getString("ComaX0ThroughX1");
                                     },
                                     desc -> getString("ComaOnlyInYearX0"),
                                     desc -> {
                                         final String specialFormat = getString("ComaYearX0ThroughYearX1");
                                         if (specialFormat == null || !specialFormat.isEmpty()) {
                                             return specialFormat;
                                         }

                                         return getString("ComaX0ThroughX1");
                                     });
    }

    /**
     * Generates the segment description
     * <p>
     * Range expressions used the 'ComaX0ThroughX1' resource
     * However Romanian language has different idioms for
     * 1. 'from number to number' (minutes, seconds, hours, days) => ComaMinX0ThroughMinX1 optional resource
     * 2. 'from month to month' ComaMonthX0ThroughMonthX1 optional resource
     * 3. 'from year to year' => ComaYearX0ThroughYearX1 optional resource
     * therefore the {@code getRangeFormat} parameter was introduced
     *
     * @param expression
     * @param allDescription
     * @param getSingleItemDescription     Functional implementation
     * @param getIntervalDescriptionFormat Functional implementation
     * @param getBetweenDescriptionFormat  Functional implementation
     * @param getDescriptionFormat         Functional implementation
     * @param getRangeFormat               Functional implementation that formats range expressions depending on cron parts
     * @return
     */
    protected String getSegmentDescription(final String expression, final String allDescription, final GetDescription getSingleItemDescription, final GetDescription getIntervalDescriptionFormat, final GetDescription getBetweenDescriptionFormat, final GetDescription getDescriptionFormat, final GetDescription getRangeFormat) {
        String description = null;

        if (expression == null || expression.isEmpty()) {
            description = "";
        } else if (expression.equals("*")) {
            description = allDescription;
        } else if (!segmentRangesOrMultipleSearchPattern.matcher(expression).find()) {
            description = String.format(getDescriptionFormat.getFor(expression), getSingleItemDescription.getFor(expression));
        } else if (expression.contains("/")) {
            final String[] segments = expression.split("/");
            description = String.format(getIntervalDescriptionFormat.getFor(segments[1]), getSingleItemDescription.getFor(segments[1]));

            //interval contains 'between' piece (i.e. 2-59/3 )
            if (segments[0].contains("-")) {
                final String betweenSegmentDescription = GenerateBetweenSegmentDescription(segments[0], getBetweenDescriptionFormat, getSingleItemDescription);
                if (!betweenSegmentDescription.startsWith(", ")) {
                    description += ", ";
                }

                description += betweenSegmentDescription;
            } else if (!segmentAnyOrMultipleSearchPattern.matcher(expression).find()) {
                // Strip any leading comma
                final String rangeItemDescription = String.format(getDescriptionFormat.getFor(segments[0]), getSingleItemDescription.getFor(segments[0])).replace(", ", "");

                description += String.format(getString("CommaStartingX0"), rangeItemDescription);
            }
        } else if (expression.contains(",")) {
            final String[] segments = expression.split(",");
            final StringBuilder descriptionContent = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if (i > 0 && segments.length > 2) {
                    descriptionContent.append(",");

                    if (i < segments.length - 1) {
                        descriptionContent.append(" ");
                    }
                }

                if (i > 0 && i == segments.length - 1) {
                    descriptionContent.append(getString("SpaceAndSpace"));
                }

                if (segments[i].contains("-")) {
                    String betweenSegmentDescription = GenerateBetweenSegmentDescription(segments[i], getRangeFormat, getSingleItemDescription);

                    //remove any leading comma
                    betweenSegmentDescription = betweenSegmentDescription.replace(", ", "");

                    descriptionContent.append(betweenSegmentDescription);
                } else {
                    descriptionContent.append(getSingleItemDescription.getFor(segments[i]));
                }
            }

            description = String.format(getDescriptionFormat.getFor(expression), descriptionContent.toString());
        } else if (expression.contains("-")) {
            description = GenerateBetweenSegmentDescription(expression, getBetweenDescriptionFormat, getSingleItemDescription);
        }

        return description;
    }

    /**
     * Generates the between segment description
     *
     * @param betweenExpression
     * @param getBetweenDescriptionFormat
     * @param getSingleItemDescription
     * @return The between segment description
     */
    protected String GenerateBetweenSegmentDescription(final String betweenExpression, final GetDescription getBetweenDescriptionFormat, final GetDescription getSingleItemDescription) {
        final String[] betweenSegments = betweenExpression.split("-");
        final String betweenSegment1Description = getSingleItemDescription.getFor(betweenSegments[0]);
        final String betweenSegment2Description = getSingleItemDescription.getFor(betweenSegments[1]).replace(":00", ":59");
        final String betweenDescriptionFormat = getBetweenDescriptionFormat.getFor(betweenExpression);

        return String.format(betweenDescriptionFormat, betweenSegment1Description, betweenSegment2Description);
    }

    /**
     * Given time parts, will contruct a formatted time description
     *
     * @param hourExpression   Hours part
     * @param minuteExpression Minutes part
     * @return Formatted time description
     */
    protected String formatTime(final String hourExpression, final String minuteExpression) {
        return formatTime(hourExpression, minuteExpression, "");
    }

    /**
     * Given time parts, will contruct a formatted time description
     *
     * @param hourExpression   Hours part
     * @param minuteExpression Minutes part
     * @param secondExpression Seconds part
     * @return Formatted time description
     */
    protected String formatTime(final String hourExpression, final String minuteExpression, final String secondExpression) {
        String period = "";

        int hour = Integer.parseInt(hourExpression);
        if (!use24HourTimeFormat) {
            period = getString((hour >= 12) ? "PMPeriod" : "AMPeriod");

            // Prepend leading space
            if (period.length() > 0) {
                period = " " + period;
            }

            // Adjust for 24 hour format
            if (hour == 0) {
                hour = 12;
            }

            if (hour > 12) {
                hour -= 12;
            }
        }

        // Zero pad and assemble time string
        final String hourString = String.format("%02d", hour);
        final String minuteString = String.format("%02d", Integer.parseInt(minuteExpression));
        String secondString = "";
        if (!secondExpression.isEmpty()) {
            secondString = ":" + String.format("%02d", Integer.parseInt(secondExpression));
        }

        return String.format("%s:%s%s%s", hourString, minuteString, secondString, period);
    }

    /**
     * Transforms the verbosity of the expression description by stripping verbosity from original description
     *
     * @param description      The description to transform
     * @param useVerboseFormat If true, will leave description as it, if false, will strip verbose parts.
     *                         The transformed description with proper verbosity
     * @return Formatted description
     */
    protected String transformVerbosity(String description, boolean useVerboseFormat) {
        if (!useVerboseFormat) {
            description = description.replace(getString("ComaEveryMinute"), "");
            description = description.replace(getString("ComaEveryHour"), "");
            description = description.replace(getString("ComaEveryDay"), "");

            description = stripDescription.replace(description);
        }

        return description;
    }

    /**
     * Gets a localized String resource
     *
     * @param resourceName
     * @return The localized string
     */
    protected String getString(final String resourceName) {
        return getString(resourceName, true);
    }

    /**
     * Gets a localized String resource, optionally returns an empty string or the requested
     * resource name if the resource is not found within the localzation packages
     *
     * @param resourceName
     * @param emptyIfNotFound
     * @return
     */
    protected String getString(final String resourceName, final boolean emptyIfNotFound) {
        try {
            return localization.getString(resourceName);
        } catch (MissingResourceException e) {
            return emptyIfNotFound ? EMPTY_STRING : "{" + resourceName + "}";
        }
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region STATIC METHODS

    /**
     * Generates a human readable String for the Cron Expression
     *
     * @param expression The cron expression String
     * @return The cron expression description
     */
    public static String getDescription(final String expression) {
        return getDescription(expression, defaultOptions);
    }

    /**
     * Generates a human readable String for the Cron Expression
     *
     * @param expression The cron expression String
     * @param options    Options to control the output description
     * @return The requested expression's description
     */
    public static String getDescription(final String expression, final Options options) {
        return new ExpressionDescriptor(expression, options).getDescription(FULL);
    }

    /**
     * Set the default locale to be used
     *
     * @param language The language identifier string for the desired locale: "en", "it", etc..
     */
    public static void setDefaultLocale(final String language) {
        defaultOptions.setLocale(language);
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
