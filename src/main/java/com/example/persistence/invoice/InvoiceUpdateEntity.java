package com.example.persistence.invoice;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(indexes = @Index(columnList = "invoice_id"))
public class InvoiceUpdateEntity {

  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Long id;
  @ManyToOne(
      fetch = FetchType.LAZY
  )
  @JoinColumn(
      name = "invoice_id"
  )
  private InvoiceEntity invoiceEntity;
  private String changeContent;
  private InvoiceUpdateType updateType;
  @CreatedDate
  private ZonedDateTime createdTime;
  @LastModifiedDate
  private ZonedDateTime modifiedTime;

  public ZonedDateTime getCreatedTime() {
    return createdTime.withZoneSameInstant(ZoneId.systemDefault());
  }

  public ZonedDateTime getModifiedTime() {
    return modifiedTime.withZoneSameInstant(ZoneId.systemDefault());
  }
}
