/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtils {

  /**
   * Util method to round the date/time with given unit.
   *
   * @param utcMillis     Date/time value to round, given in utc millis
   * @param unitMillis    Date/time interval unit in utc millis
   * @return              Rounded date/time value in utc millis
   */
  public static long roundFloor(long utcMillis, long unitMillis) {
    return utcMillis - utcMillis % unitMillis;
  }

  /**
   * Util method to round the date/time in week(s).
   *
   * @param utcMillis     Date/time value to round, given in utc millis
   * @param interval      Number of weeks as the rounding interval
   * @return              Rounded date/time value in utc millis
   */
  public static long roundWeek(long utcMillis, int interval) {
    return roundFloor(utcMillis + 259200000L, 604800000L * interval) - 259200000L;
  }

  /**
   * Util method to round the date/time in month(s).
   *
   * @param utcMillis     Date/time value to round, given in utc millis
   * @param interval      Number of months as the rounding interval
   * @return              Rounded date/time value in utc millis
   */
  public static long roundMonth(long utcMillis, int interval) {
    ZonedDateTime initDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC"))
        .plusMonths(interval);
    long monthDiff = (zonedDateTime.getYear() - initDateTime.getYear()) * 12L + zonedDateTime
        .getMonthValue() - initDateTime.getMonthValue();
    long monthToAdd = (monthDiff / interval - 1) * interval;
    return initDateTime.plusMonths(monthToAdd).toInstant().toEpochMilli();
  }

  /**
   * Util method to round the date/time in quarter(s).
   *
   * @param utcMillis     Date/time value to round, given in utc millis
   * @param interval      Number of quarters as the rounding interval
   * @return              Rounded date/time value in utc millis
   */
  public static long roundQuarter(long utcMillis, int interval) {
    ZonedDateTime initDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC"))
        .plusMonths(interval * 3L);
    long monthDiff = ((zonedDateTime.getYear() - initDateTime.getYear()) * 12L + zonedDateTime
        .getMonthValue() - initDateTime.getMonthValue());
    long monthToAdd = (monthDiff / (interval * 3L) - 1) * interval * 3;
    return initDateTime.plusMonths(monthToAdd).toInstant().toEpochMilli();
  }

  /**
   * Util method to round the date/time in year(s).
   *
   * @param utcMillis     Date/time value to round, given in utc millis
   * @param interval      Number of years as the rounding interval
   * @return              Rounded date/time value in utc millis
   */
  public static long roundYear(long utcMillis, int interval) {
    ZonedDateTime initDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC"));
    int yearDiff = zonedDateTime.getYear() - initDateTime.getYear();
    int yearToAdd = (yearDiff / interval) * interval;
    return initDateTime.plusYears(yearToAdd).toInstant().toEpochMilli();
  }

  /**
   * Get window start time which aligns with the given size.
   *
   * @param timestamp event timestamp
   * @param size defines a window's start time to align with
   * @return start timestamp of the window
   */
  public long getWindowStartTime(long timestamp, long size) {
    return timestamp - timestamp % size;
  }

  /**
   * isValidMySqlTimeZoneId for timezones which match timezone the range set by MySQL.
   *
   * @param zone ZoneId of ZoneId type.
   * @return Boolean.
   */
  public Boolean isValidMySqlTimeZoneId(ZoneId zone) {
    String timeZoneMax = "+14:00";
    String timeZoneMin = "-13:59";
    String timeZoneZero = "+00:00";

    ZoneId maxTz = ZoneId.of(timeZoneMax);
    ZoneId minTz = ZoneId.of(timeZoneMin);
    ZoneId defaultTz = ZoneId.of(timeZoneZero);

    ZonedDateTime defaultDateTime = LocalDateTime.of(2000, 1, 2, 12, 0).atZone(defaultTz);

    ZonedDateTime maxTzValidator =
        defaultDateTime.withZoneSameInstant(maxTz).withZoneSameLocal(defaultTz);
    ZonedDateTime minTzValidator =
        defaultDateTime.withZoneSameInstant(minTz).withZoneSameLocal(defaultTz);
    ZonedDateTime passedTzValidator =
        defaultDateTime.withZoneSameInstant(zone).withZoneSameLocal(defaultTz);

    return (passedTzValidator.isBefore(maxTzValidator)
        || passedTzValidator.isEqual(maxTzValidator))
        && (passedTzValidator.isAfter(minTzValidator)
        || passedTzValidator.isEqual(minTzValidator));
  }
}
