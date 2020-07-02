**Version 1.0.3-SNAPSHOT**
 * Fixed verbose mode description stripper
 * Verbose mode is now OFF by default in the Options class
 * Removed "dayOfWeekStartIndexZero" from the Options class, we can infer based on our standards' research whether we should wrap Week Days around at 7 or not. For example: Regular cron (5 parts) does this, albeit not standard, but Quartz (which uses 6 or 7 parts) never does. 
 
   This option is problematic to pick a default for anyway, since it requires knowledge of the standards employed by the cron system in use. Errors in judgement can cause the description to provide a wrong day name and lead to scheduling issues. By taking on the responsibility to validate this behavior we spare implementors the research and offer a better functionality out of the box. 
 
   A forced override via options might be introduced in the future, if need arises.

**Version 1.0.2-SNAPSHOT**
 * Fixed parser not validating YEAR ranges and single values

**Version 1.0.1-SNAPSHOT**
 * The ``` CronExpressionDescriptor ``` class now offers an empty constructor and methods to set the expression and options to be used
   on the next call to the ``` getDescription() ``` methods. This allows for object reuse in order to avoid needless allocations.
 * The parsing stage will now throw helpful exceptions that allow implementations to deliver feedback to the user based on the specific 
   error detected.
   
**Version 1.0**
 * Initial release