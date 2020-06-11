package com.burning.cron;

import java.util.Locale;

public class ExpressionParserOptions {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region FIELDS

    private boolean throwExceptionOnParseError = true;
    private boolean verbose                    = false;
    private boolean dayOfWeekStartIndexZero    = true;
    private boolean use24HourTimeFormat        = false;
    private Locale  locale                     = Locale.getDefault();

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    public boolean isUse24HourTimeFormat() {
        return use24HourTimeFormat;
    }

    public void setUse24HourTimeFormat(boolean use24HourTimeFormat) {
        this.use24HourTimeFormat = use24HourTimeFormat;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region CONSTRUCTORS

    /**
     * Constructor
     */
    public ExpressionParserOptions() {

    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
