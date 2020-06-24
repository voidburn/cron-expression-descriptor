**Version 1.0.1**
 * The ``` CronExpressionDescriptor ``` class now offers an empty constructor and methods to set the expression and options to be used
   on the next call to the ``` getDescription() ``` methods. This allows for object reuse in order to avoid needless allocations.
 
