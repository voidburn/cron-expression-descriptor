package it.burning.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RxReplace {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region FIELDS

    private final Pattern pattern;
    private       Matcher matcher;

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region CONSTRUCTORS

    /**
     * Constructor
     *
     * @param regex The regular expression to use for replacement
     */
    public RxReplace(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //region METHODS

    /**
     * Overridden to compute a replacement for each match. Use the method 'group' to access the captured groups.
     *
     * @return String The replaced string
     */
    public abstract String replacement();

    /**
     * Returns the input subsequence captured by the given group during the previous match operation.
     *
     * @param i The group index (starting at 0)
     * @return The contents of the requested group
     */
    public String group(int i) {
        return matcher.group(i);
    }

    /**
     * Returns the result of rewriting 'original' by invoking the method 'replacement' for each match of the regular expression supplied to the constructor
     *
     * @param original The original string
     * @return The rewritten string after replacements
     */
    public String replace(final CharSequence original) {
        // Get a matcher for the original pattern
        this.matcher = pattern.matcher(original);

        final StringBuffer result = new StringBuffer(original.length());
        while (matcher.find()) {
            // Discard everything up until the current match (we just want to update the matcher's cursor)
            matcher.appendReplacement(result, "");

            // Perform implemented replacement and append the resulting string to the buffer
            result.append(replacement());
        }

        // Append the rest of the sequence
        matcher.appendTail(result);

        // Return the rewritten string
        return result.toString();
    }

    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
