package com.example.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuarterTest {

  @Test
  void testQuarterName() {
    ZonedDateTime startDate = ZonedDateTime.parse("2023-12-09T15:30:45.123+07:00");
    ZonedDateTime endDate =  ZonedDateTime.parse("2024-01-09T15:30:45.123+07:00");
    Quarter quarter = new Quarter(startDate, endDate);
    assertEquals("Q4-2023",quarter.getName());
  }


  @Test
  void getQuarterListFromCurrent() {
    var test = ZonedDateTime.parse("2024-12-10T15:30:45.123+07:00");
    List<String> quarterList = Quarter.getQuarterList(test);
    assertEquals(8, quarterList.size());
    assertEquals("Q4-2024", quarterList.get(0));
    assertEquals("Q3-2024", quarterList.get(1));
    assertEquals("Q2-2024", quarterList.get(2));
    assertEquals("Q1-2024", quarterList.get(3));
    assertEquals("Q4-2023", quarterList.get(4));
    assertEquals("Q3-2023", quarterList.get(5));
    assertEquals("Q2-2023", quarterList.get(6));
    assertEquals("Q1-2023", quarterList.get(7));


  }
}