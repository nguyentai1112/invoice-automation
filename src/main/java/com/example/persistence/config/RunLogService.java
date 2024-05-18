package com.example.persistence.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RunLogService {
  private final RunLogRepository runLogRepository;
  private final DataSource dataSource;



  @Value("${config.startTime}")
  private String startTimeStr;

  public RunLogService(DataSource datasource, RunLogRepository runLogRepository) {
    this.dataSource = datasource;
    this.runLogRepository = runLogRepository;
  }

  public ZonedDateTime getLastCrawledTime() {

    return runLogRepository.findFirstByOrderByLastCrawledTimeDesc().get().getLastCrawledTime();
  }

  public RunLogEntity getConfig() {

    return runLogRepository.findFirstByOrderByLastCrawledTimeDesc().get();
  }

  public List<RunLogEntity> getAll() {

    return runLogRepository.findTop200ByOrderByRunTimeDesc();
  }

  @PostConstruct
  public void initConfig() {
    if (runLogRepository.findFirstByOrderByLastCrawledTimeDesc().isEmpty()) {
      RunLogEntity runLogEntity = new RunLogEntity();
      //  ZonedDateTime defaultZoneDateTime = ZonedDateTime.now().minusYears(1).truncatedTo(
      //    ChronoUnit.DAYS).withDayOfYear(1);
    //  ZonedDateTime defaultZoneDateTime = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0,
      //    ZoneId.systemDefault());
      ZonedDateTime defaultZoneDateTime = ZonedDateTime.parse(startTimeStr).withZoneSameInstant(ZoneId.systemDefault());
      runLogEntity.setLastCrawledTime(defaultZoneDateTime);
      runLogEntity.setRunTime(ZonedDateTime.now());
      runLogEntity.setStartTime(defaultZoneDateTime);
      runLogEntity.setEndTime(defaultZoneDateTime);
      runLogEntity.setModifiedTime(ZonedDateTime.now());
      runLogEntity.setProcessCount(0);
      runLogEntity.setDuration(0L);

      runLogRepository.save(runLogEntity);
      log.info("Init config with last crawled time: {}", runLogEntity.getLastCrawledTime());
    }

  }

  @Transactional
  public void updateLastCrawledTime(ZonedDateTime lastCrawledTime, long duration) {
    RunLogEntity runLogEntity = runLogRepository.findFirstByOrderByLastCrawledTimeDesc().get();
    runLogEntity.setLastCrawledTime(lastCrawledTime);
    runLogEntity.setRunTime(ZonedDateTime.now());
    runLogEntity.setDuration(duration);
    runLogRepository.save(runLogEntity);
    log.info("Update last crawled time {} and duration: {}", lastCrawledTime, duration);
  }

  @Transactional
  public void save(RunLogEntity runLogEntity) {
    runLogEntity.setModifiedTime(ZonedDateTime.now());
    runLogRepository.save(runLogEntity);
    log.info("Update last crawled time {} and duration: {}", runLogEntity.getLastCrawledTime(),
        runLogEntity.getDuration());
  }


  @Transactional
  public void removeOldLogs() {
    //deleteOlderThanOneWeek
    ZonedDateTime cutoffTime = ZonedDateTime.now().minus(1, ChronoUnit.WEEKS);
    runLogRepository.deleteByLastCrawledTimeBefore(cutoffTime);
  }


}
