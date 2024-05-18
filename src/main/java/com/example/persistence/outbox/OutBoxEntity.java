package com.example.persistence.outbox;

import com.example.dto.Email;
import com.example.persistence.receiver.ReceiverEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OutBoxEntity {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Long id;
  @Column(unique=true, updatable = false)
  private String messageId;
  private String title;
  private ZonedDateTime sentTime;
  private String body;
  @ManyToOne(
      fetch = FetchType.EAGER
  )
  @JoinColumn(
      name = "email"
  )
  private ReceiverEntity receiver;
  private String originalRawReceiver;
  private String cc;
  private Long internalDate;
  private String threadId;
  private Integer invoiceNum;
  private String failedInvoices;
  @CreatedDate
  private ZonedDateTime createdTime;
  @LastModifiedDate
  private ZonedDateTime modifiedTime;

  public ZonedDateTime getSentTime() {
    return sentTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  public ZonedDateTime getCreatedTimeStr() {
    return createdTime.withZoneSameInstant(ZoneId.systemDefault());
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  public ZonedDateTime getModifiedTimeStr() {
    return modifiedTime.withZoneSameInstant(ZoneId.systemDefault());
  }


}
