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
     * @param regex
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
     */
    public abstract String replacement();

    /**
     * Returns the input subsequence captured by the given group during the previous match operation.
     *
     * @param i
     * @return
     */
    public String group(int i) {
        return matcher.group(i);
    }

    /**
     * Returns the result of rewriting 'original' by invoking the method 'replacement' for each match of the regular expression supplied to the constructor
     *
     * @param original
     * @return
     */
    public String replace(final CharSequence original) {
        // Get a matcher for the original pattern
        this.matcher = pattern.matcher(original);

        final StringBuffer result = new StringBuffer(original.length());
        while (matcher.find()) {
            // Load the next match into our buffer without altering it
            matcher.appendReplacement(result, "");

            // Perform implemented replacement
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
