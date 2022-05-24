# Cron Expression Descriptor
A Java library that converts cron expressions into human readable descriptions. Adapted from original work by Brady Holt (https://github.com/bradymholt/cron-expression-descriptor)

![Build](https://github.com/voidburn/cron-expression-descriptor/workflows/Build/badge.svg)

## License
Licensed under the [MIT](https://github.com/voidburn/cron-expression-descriptor/LICENSE) license 

## Features

 * Supports all cron expression special characters including * / , - ? L W, #
 * Supports 5, 6 (w/ seconds or year), or 7 (w/ seconds and year) part cron expressions
 * Supports [Quartz Enterprise Scheduler](https://www.quartz-scheduler.net/) cron expressions
 * Localization with support for 22 languages
 
## Add it to your project!

### Maven
Add the following dependency to your pom.xml:

```
<dependency>
  <groupId>it.burning</groupId>
  <artifactId>cron-expression-descriptor</artifactId>
  <version>1.2.4</version>
</dependency>
```

### Gradle
Add the repositories and dependency to your gradle.build script:

```
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    maven { url "https://oss.sonatype.org/content/repositories/releases/" }
}

dependencies {
    implementation "it.burning:cron-expression-descriptor:1.2.4"
}
```
 
 ## Options
 
 A `ExpressionDescriptor.Options` object can be passed to `getDescription()`.  The following options are available:
 
 - **boolean throwExceptionOnParseError** - If exception occurs when trying to parse expression and generate description, whether to throw or catch and output the Exception message as the description. **(Default: true)**
 - **boolean verbose** - Whether to use a verbose description **(Default: false)**
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

* English - `en` ([Brady Holt](https://github.com/bradymholt))
* Chinese Simplified - zh-Hans `zh-CN` ([Star Peng](https://github.com/starpeng))
* Chinese Traditional - zh-Hant `zh-TW` ([Ricky Chiang](https://github.com/metavige))
* Danish - `da` ([Rasmus Melchior Jacobsen](https://github.com/rmja))
* Dutch - `nl` ([TotalMace](https://github.com/TotalMace))
* Finnish - `fi` ([Mikael Rosenberg](https://github.com/MR77FI))
* French - `fr` ([Arnaud TAMAILLON](https://github.com/Greybird))
* German - `de` ([Michael Schuler](https://github.com/mschuler))
* Italian - `it` ([Luca Vignaroli](https://github.com/voidburn))
* Japanese - `ja` ([Alin Sarivan](https://github.com/asarivan))
* Korean - `ko` ([Ion Mincu](https://github.com/ionmincu))
* Norwegian - `nb` ([Siarhei Khalipski](https://github.com/KhalipskiSiarhei))
* Polish - `pl` ([foka](https://github.com/foka))
* Portuguese `pt` ([Miguel Guimarães](https://github.com/hmiguim))
* Portuguese - Brazil `pt-BR` ([Renato Lima](https://github.com/natenho))
* Romanian - `ro` ([Illegitimis](https://github.com/illegitimis))
* Russian - `ru` ([LbISS](https://github.com/LbISS))
* Slovenian - `sl-SI` ([Jani Bevk](https://github.com/jenzy))
* Spanish - `es` ([Ivan Santos](https://github.com/ivansg))
* Spanish -Mexico `es-MX` ([Ion Mincu](https://github.com/ionmincu))
* Swedish - `sv` ([roobin](https://github.com/roobin))
* Turkish - `tr` ([Mustafa SADEDİL](https://github.com/sadedil))
* Ukrainian - `uk` ([Taras](https://github.com/tbudurovych))

If you want to manually set a default Locale for the descripion, to be used for all subsequent calls to "
getDescription()" you can use the static method "setDefaultLocale()" by passing it the language identifier:

 ```java
 ExpressionDescriptor.setDefaultLocale("it");
 ExpressionDescriptor.getDescription("0-10 11 * * *");
 > "Alle 12:00, ogni giorno"
```

And you can revert at any time to the default Locale:

For Version 1.2.3+

 ```java
 ExpressionDescriptor.setDefaultLocale();
 ExpressionDescriptor.getDescription("0-10 11 * * *");
  > "At 12:00, every day"
 ```

Up until Version 1.2.2

 ```java
 ExpressionDescriptor.setDefaultLocale(null);
 ExpressionDescriptor.getDescription("0-10 11 * * *");
  > "At 12:00, every day"
 ```

