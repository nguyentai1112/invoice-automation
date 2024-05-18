package com.example.scheduler;

import com.example.persistence.config.RunLogEntity;
import com.example.persistence.config.RunLogService;
import com.example.persistence.outbox.OutBoxService;
import com.example.service.ProcessorService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class Crawler {

  private final ProcessorService processorService;
  private final RunLogService runLogService;
  private final OutBoxService outBoxService;

  //@Scheduled(fixedRate = 1, initialDelay = 0, timeUnit = TimeUnit.MINUTES)
  @Scheduled(fixedRateString = "${schedule.fixedRate}", initialDelayString = "${schedule.initialDelay}", timeUnit = TimeUnit.MINUTES)

  public void scheduleFixedRateWithInitialDelayTask() throws Exception {
    ZonedDateTime latestCrawledTime = runLogService.getLastCrawledTime()
        .withZoneSameInstant(ZoneId.systemDefault());
    //the end of the day
    ZonedDateTime nextTime = latestCrawledTime
        .plusDays(1).truncatedTo(ChronoUnit.DAYS);
    try {
      long startTime = System.currentTimeMillis();
      RunLogEntity runLogEntity = RunLogEntity.builder().build();
      runLogEntity.setStartTime(latestCrawledTime);
      runLogEntity.setEndTime(nextTime);
      runLogEntity.setRunTime(ZonedDateTime.now());
      log.info("Received schedule request to process emails from {} to {}", latestCrawledTime, nextTime);
      int processedNum = processorService.process(latestCrawledTime, nextTime);
      long duration = System.currentTimeMillis() - startTime;
      log.info("Total process time: {} seconds", processedNum, duration / 1000l);
      ZonedDateTime latestSentTime = outBoxService.getLatestSentTime();

      runLogEntity.setProcessCount(processedNum);
      runLogEntity.setDuration(duration);
      if (processedNum > 0) {
        //configService.updateLastCrawledTime(latestSentTime, duration);
        runLogEntity.setLastCrawledTime(latestSentTime);
      } else { // no processed data in that day
        if (latestCrawledTime.isBefore(latestSentTime)) {
          //  configService.updateLastCrawledTime(latestSentTime, duration);
          runLogEntity.setLastCrawledTime(latestSentTime);
        } else {
          if (nextTime.isBefore(ZonedDateTime.now())) {
            //configService.updateLastCrawledTime(nextTime, duration);
            runLogEntity.setLastCrawledTime(nextTime);
          } else {
            runLogEntity.setLastCrawledTime(latestCrawledTime);
          }
        }
      }
      runLogService.save(runLogEntity);

      runLogService.removeOldLogs();

    } catch (Exception e) {
      log.error("Error when processing emails by schedule", e);
    }

  }
}
