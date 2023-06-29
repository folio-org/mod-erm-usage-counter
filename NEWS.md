## 2.2.8
* Bump guava from 30.1.1-jre to 32.0.1-jre
* [MODEUSCNT-45](https://issues.folio.org/browse/MODEUSCNT-45) Update to latest counter openapi specification

## 2.2.7

* [MODEUSCNT-35](https://issues.folio.org/browse/MODEUSCNT-35) Update dependencies with vulnerabilities
* [MODEUSCNT-33](https://issues.folio.org/browse/MODEUSCNT-33) Replace retired Apache Oltu
* [MODEUSCNT-32](https://issues.folio.org/browse/MODEUSCNT-32) Okhttp 3 Information Exposure from illegal character in a header
* [MODEUSCNT-31](https://issues.folio.org/browse/MODEUSCNT-31) Remove unused dependencies

## 2.2.6

* [MODEUSCNT-30](https://issues.folio.org/browse/MODEUSCNT-30) Update title matching in csv mapper
* [MODEUSCNT-29](https://issues.folio.org/browse/MODEUSCNT-29) Missing report name/title definitions

## 2.2.5

* [MODEUSCNT-27](https://issues.folio.org/browse/MODEUSCNT-27) Report date only updated when a performance period exists
* [MODEUSCNT-26](https://issues.folio.org/browse/MODEUSCNT-26) Publish javadoc and sources to maven repository

## 2.2.4

* [MODEUSCNT-25](https://issues.folio.org/browse/MODEUSCNT-25) Conflicting jackson versions

## 2.2.3

* [MODEUSCNT-24](https://issues.folio.org/browse/MODEUSCNT-24) Update depencendies
* [MODEUSCNT-23](https://issues.folio.org/browse/MODEUSCNT-23) commons-beanutils Arbitrary Code Execution (CVE-2014-0114)
* [MODEUSCNT-22](https://issues.folio.org/browse/MODEUSCNT-22) Remove spring-context dependency

## 2.2.2

* [MODEUSCNT-20](https://issues.folio.org/browse/MODEUSCNT-20) Reading data from CSV reports results in invalid JSON report data

## 2.2.1

* [MODEUSCNT-19](https://issues.folio.org/browse/MODEUSCNT-19) Fix wrong column type used in PRCsvToReport.java

## 2.2.0

* [MODEUSCNT-17](https://issues.folio.org/browse/MODEUSCNT-17) Security vulnerabilites CVE-2021-21429, CVE-2021-21430
* [MODEUSCNT-16](https://issues.folio.org/browse/MODEUSCNT-16) Incorrect cell formatting in xlsx
* [MODEUSCNT-15](https://issues.folio.org/browse/MODEUSCNT-15) NullPointerException when converting master reports into standard views
* [MODEUSCNT-13](https://issues.folio.org/browse/MODEUSCNT-13) CSV files not recognized as report if the first line contains quotes
* [MODEUSCNT-11](https://issues.folio.org/browse/MODEUSCNT-11) Create csv mappings for Standard Report Views

## 2.1.0

* Add functionality for converting Master Reports into Standard Report
  Views ([MODEUSCNT-10](https://issues.folio.org/browse/MODEUSCNT-10))

## 2.0.2

* [MODEUSCNT-9](https://issues.folio.org/browse/MODEUSCNT-9) Update Counter Sushi 5.0 API
  Specification
* Bump Guava from 29.0-jre to 31.1.1-jre

## 2.0.1

* XMLGregorianCalendar serialization/deserialization mismatch (MODEUSCNT-7)

## 2.0.0

* Update to JDK11 (MODEUSCNT-3)
* Generating COP5 report from csv fails (MODEUSCNT-4)
* Add missing mappingEntries to Counter4Utils (MODEUSCNT-5)

## 1.7.0

* counter41: Add toXML() functions (MODEUSCNT-1)

## 1.6.0

* Add functions for converting csv to xlsx and vice versa (MODEUS-60)

## 1.5.0

* Add COP5 CSV mappings for multiple months (json & csv) (UIEUS-162 & MODEUS-54)

## 1.4.0

* Add COP5 CSV mappings for single months (UIEUS-106)

## 1.3.0

* counter41: Do not check vendor attributes when merging reports
* counter41: When splitting a report, do not include a ReportItem in a month when there is no
  ItemPerformance for this month
* counter41: Add method for splitting a report into separate months
* Update jackson-databind to 2.10.0
* counter41: Add report mappings for csv (JR1, PR1, DB1, BR1, BR2)
* counter41: Fix JR1 mapping incorrect DOI and PROPRIETARY ItemIdentifiers
* counter41: Dont create empty lines in csv

## 1.2.3

* Update jackson-databind to 2.9.9.3 CVE-2019-14379, CVE-2019-14439

## 1.2.2

* Update jackson-databind to 2.9.9.1 CVE-2019-12814

## 1.2.1

* counter41: Fix counting in MonthPerformanceProcessor
* counter41: Fix possible NPE when getting error messages

## 1.2.0

* Add Counter5Utils

## 1.1.0

* Add COP4 CSV mappings for reports BR1, BR2, DB1, PR1
* Add model and client for COUNTER/SUSHI 5.0

## 1.0.0

* generated model and client for COUNTER/SUSHI 4.1
* utility functions for working with reports, e.g. XML/JSON/CSV conversion
