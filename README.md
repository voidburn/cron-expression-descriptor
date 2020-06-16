# Cron Expression Descriptor
A Java library that converts cron expressions into human readable descriptions. Adapted from original work by Brady Holt (https://github.com/bradymholt/cron-expression-descriptor)

**Licensed under the [MIT](https://github.com/voidburn/cron-expression-descriptor/LICENSE) ** 

![Build](https://github.com/voidburn/cron-expression-descriptor/workflows/Build/badge.svg)

## Features

 * Supports all cron expression special characters including * / , - ? L W, #
 * Supports 5, 6 (w/ seconds or year), or 7 (w/ seconds and year) part cron expressions
 * Localization support
 * Provides casing options (Sentence case, Title Case, lowercase, etc.)
 * Supports [Quartz Enterprise Scheduler .NET](https://www.quartz-scheduler.net/) cron expressions
 
 ## Options
 
 A `ExpressionDescriptor.Options` object can be passed to `getDescription()`.  The following options are available:
 
 - **boolean throwExceptionOnParseError** - If exception occurs when trying to parse expression and generate description, whether to throw or catch and output the Exception message as the description. **(Default: true)**
 - **boolean verbose** - Whether to use a verbose description **(Default: true)**
 - **boolean dayOfWeekStartIndexZero** - Whether to interpret cron expression DOW `1` as Sunday or Monday. **(Default: false)**
 - **boolean use24HourTimeFormat** - If true, descriptions will use a [24-hour clock](https://en.wikipedia.org/wiki/24-hour_clock) **(Default: true)**
 - **string locale** - The locale to use **(Default: current system locale)**
 
 Example usage with default options:
 
 ```java
ExpressionDescriptor.getDescription("0 0 12 * * ?");
> "At 12:00, every day"
 ```
 Example usage with custom options:
  
 ```java
ExpressionDescriptor.getDescription("0 0 12 * * ?", new Options() {{ 
    setLocale("it");
    setUse24HourTimeFormat(false);
}});
> "Alle 12:00 PM, ogni giorno"
  ```
 
 **Please Note**: Default options are cached internally, but if you want to use a custom Options set it is advisable to instantiate it only once and reuse it on every
 subsequent call to avoid useless allocation.
 
 ## i18n
 
 The following language translations are available.
 
  * English - en ([Brady Holt](https://github.com/bradymholt))
  * Italian - it ([Luca Vignaroli](https://github.com/voidburn))
  
 If you want to manually set a default Locale for the descripion, to be used for all subsequent calls to "getDescription()" you can use the static method "setDefaultLocale()" by passing it the language identifier:
 
 ```java
 ExpressionDescriptor.setDefaultLocale("it");
 ExpressionDescriptor.getDescription("0-10 11 * * *");
 > "Alle 12:00, ogni giorno"
```

 And you can revert at any time to the default Locale (system's language, assuming english here):
 ```java
 ExpressionDescriptor.setDefaultLocale(null);
 ExpressionDescriptor.getDescription("0-10 11 * * *");
  > "At 12:00, every day"
 ```
