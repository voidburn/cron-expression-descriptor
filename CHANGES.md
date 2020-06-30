**Version 1.0.2-SNAPSHOT**
 * Fixed parser not validating YEAR ranges and single values

**Version 1.0.1-SNAPSHOT**
 * The ``` CronExpressionDescriptor ``` class now offers an empty constructor and methods to set the expression and options to be used
   on the next call to the ``` getDescription() ``` methods. This allows for object reuse in order to avoid needless allocations.
 * The parsing stage will now throw helpful exceptions that allow implementations to deliver feedback to the user based on the specific 
   error detected.
   
**Version 1.0**
 * Initial release