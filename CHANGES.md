**Version 1.2.10**
* Fix for French localization

**Version 1.2.9**
* French localization improvements and fixes by [Erwan Garreau](https://github.com/ErwanGarreau)

**Version 1.2.8**
* Added Bulgarian localization by [Angel Gospodinov](https://github.com/AngloIBS)

**Version 1.2.7**

* Added Vietnamese localization by [Duong Van Minh](https://github.com/eddyduong3010)

**Version 1.2.6**

* Added support for JEE Schedule format (https://docs.oracle.com/javaee/7/tutorial/ejb-basicexamples004.htm) via config
  option

**Version 1.2.5**

* Merged Portuguese localization by Miguel Guimarães which moved the existing localization for Portuguese to pt-BR (
  Brazil)

**Version 1.2.4**

* Setting a locale with a string descriptor now uses the Locale.forLanguageTag(descriptor) method.
* All resource files are now forcibly read as UTF-8, this should fix display issues with many languages (See issue #3
  for example)
* Added descriptor tests for the Portuguese language.

**Version 1.2.3**

**BREAKING CHANGE**: To reset the default locale now you must invoke the method without passing "
null": `ExpressionDescriptor.setDefaultLocale();`

* Fixed issue that prevented setting locales with more than two letter descriptors (e.g. "zh_CN", "es_MX"..)
* Added new overloads to the `setDefaultLocale` method to allow passing in Locale constants directly, for
  example: `ExpressionDescriptor.setDefaultLocale(Locale.ENGLISH);`

**Version 1.2.2**

* Additional fixes for the Italian localization file

**Version 1.2.1**

* Fixed Italian localization file

**Version 1.2**

* Fixed detection of 6 part expressions when ```*``` is specified for the year

**Version 1.1**

* Release to Maven Central

**Version 1.0.3-SNAPSHOT**

* Fixed verbose mode description stripper
* Verbose mode is now OFF by default in the Options class
* Removed "dayOfWeekStartIndexZero" from the Options class, we can infer based on our standards' research whether we
  should wrap Week Days around at 7 or not. For example: Regular cron (5 parts) does this, albeit not standard, but
  Quartz (which uses 6 or 7 parts) never does.

  This option is problematic to pick a default for anyway, since it requires knowledge of the standards employed by the
  cron system in use. Errors in judgement can cause the description to provide a wrong day name and lead to scheduling
  issues. By taking on the responsibility to validate this behavior we spare implementors the research and offer a
  better functionality out of the box.

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
