package com.example.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Quarter {

  private static final DateTimeFormatter QUARTER_NAME_FORMAT = DateTimeFormatter.ofPattern(
      "'Q'Q-yyyy");
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;
  private String name;

  public Quarter(ZonedDateTime startDate, ZonedDateTime endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.name = getQuarterName(this.startDate);
  }

  public Quarter(String startDateString, String endDateString) {
    this.startDate = ZonedDateTime.parse(startDateString);
    this.endDate = ZonedDateTime.parse(endDateString);
    this.name = getQuarterName(this.startDate);

  }

  public static String getQuarterName(LocalDate startDate) {
    return QUARTER_NAME_FORMAT.format(startDate);
  }

  public static String getQuarterName(ZonedDateTime startDate) {
    return QUARTER_NAME_FORMAT.format(startDate);
  }


  public static String getQuarterName(String dateString) {
    return getQuarterName(LocalDate.parse(dateString, Invoice.INVOICE_DATE_FORMAT));
  }

  public static Quarter fromString(String quarterString) {
    int quarter = Integer.parseInt(quarterString.substring(1, 2));
    int year = Integer.parseInt(quarterString.substring(3));

    // Create a ZonedDateTime for the first day of the quarter
    ZonedDateTime from = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    //the end of the quater
    ZonedDateTime to = ZonedDateTime.of(year, quarter * 3, 1, 0, 0, 0, 0,
        ZoneId.systemDefault()).plusMonths(1).minusSeconds(1);

    return new Quarter(from, to);
  }

  public static List<String> getQuarterList(ZonedDateTime now) {
    List<String> result = new ArrayList<>();
    //get the first day of the last year;
    ZonedDateTime lastYear = now.minusYears(1).withMonth(1).withDayOfMonth(1);
    while (lastYear.isBefore(now)) {
      result.add(getQuarterName(now));
      now = now.minusMonths(3);
    }
    return result;
  }
}
