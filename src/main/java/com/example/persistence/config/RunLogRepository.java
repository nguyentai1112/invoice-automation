package com.example.persistence.config;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RunLogRepository extends JpaRepository<RunLogEntity, Long> {
  Optional<RunLogEntity> findFirstByOrderByLastCrawledTimeDesc();
  List<RunLogEntity> findTop200ByOrderByRunTimeDesc();

  @Modifying
  @Query("DELETE FROM RunLogEntity r WHERE r.runTime < :cutoffTime")
  void deleteByLastCrawledTimeBefore(@Param("cutoffTime") ZonedDateTime cutoffTime);
}
