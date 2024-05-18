package com.example.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilTest {

  @ParameterizedTest
  @CsvSource( {
      "846.720, 846720",
      "678, 678",
      "70.000 , 70000 ",
      "1, 1", // Additional test case
      "-10, -10" // Additional test case
  })
  public void testStringNumToInt(String stringNum, String expected) {

    long result = StringUtil.stringNumToLong(stringNum);
    long expectedLong = Long.parseLong(expected);

    assertEquals(expectedLong, result);
  }


  @Test
  void stringPercenttoInt() {
  }

  //Ngày (day) 30 tháng (month) 11 năm (year) 2023
  //Ngày (Date) 30 tháng (month) 11 năm (year) 2023
  @Test
  void testExtractDate() {
  }

  @Test
  void extractFromPattern() {
  }

  @Test
  void removeSpaces() {
  }

  @Test
  void generateFileName() {
    String email = "tai@gmail.com";
    String actual = StringUtil.generateFileName(email);
    String expectedRegex = "tai@gmail\\.com_\\d{8}_\\d{6}";
    assertTrue(actual.matches(expectedRegex));

  }

  @Test
  void test(){
    String dateString = "12-12-2022";


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDate localDate = LocalDate.parse(dateString, formatter);
    System.out.println(ZonedDateTime.now());

    System.out.println(localDate);
  }


  @Test
  void convertGmailTimeToZoneDateTime() {
    String gmailTime = "Thu, 16 Dec 2021 09:00:00 +0700";
    ZonedDateTime actual = StringUtil.convertGmailTimeToZoneDateTime(gmailTime);
    ZonedDateTime expected = ZonedDateTime.of(2021, 12, 16, 9, 0, 0, 0, ZoneOffset.of("+07:00"));

    assertEquals(expected, actual);

     gmailTime = "Fri, 1 Dec 2023 22:59:32 +0700";
     actual = StringUtil.convertGmailTimeToZoneDateTime(gmailTime);
     expected = ZonedDateTime.of(2023, 12, 1, 22, 59, 32, 0, ZoneOffset.of("+07:00"));

    assertEquals(expected, actual);
  }
  @Test
  public void testTime(){
    ZonedDateTime defaultZoneDateTime = ZonedDateTime.now().minusYears(1).truncatedTo(
        ChronoUnit.DAYS).withDayOfYear(1);
    System.out.println(defaultZoneDateTime);

    ZonedDateTime nextTime = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);

    System.out.println(nextTime);
    ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

    // Convert to GMT+7
    ZonedDateTime gmtPlus7DateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
    ZonedDateTime test = ZonedDateTime.of(2023,4,1,0,0,0,0, ZoneId.systemDefault());

    System.out.println("UTC: " + utcDateTime);
    System.out.println("GMT+7: " + gmtPlus7DateTime);
    System.out.println(test);
  }
}