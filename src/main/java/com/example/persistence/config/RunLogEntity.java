package com.example.persistence.config;

import com.example.util.StringUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunLogEntity {

  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Long id;
  private ZonedDateTime lastCrawledTime;
  private ZonedDateTime runTime;
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;
  private ZonedDateTime modifiedTime;

  private Integer processCount;
  private Long duration;


  public ZonedDateTime getModifiedTime() {
    return modifiedTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  public ZonedDateTime getLastCrawledTime() {
    return lastCrawledTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  public ZonedDateTime getRunTime() {
    return runTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  public ZonedDateTime getStartTime() {
    return startTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  public ZonedDateTime getEndTime() {
    return endTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  public String getStartTimeString() {
    return StringUtil.displayDateTime(getStartTime());
  }
  public String getEndTimeString() {
    return StringUtil.displayDateTime(getEndTime());
  }
  public String getRunTimeString() {
    return StringUtil.displayDateTime(getRunTime());
  }
  public String getLastCrawledTimeString() {
    return StringUtil.displayDateTime(getLastCrawledTime());
  }
}
