package com.example.persistence.receiver;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverEntity {
  @Id
  @Column(unique=true, updatable = false)
  String email;
  String name;
  @CreatedDate
  private ZonedDateTime createdTime;
  @LastModifiedDate
  private ZonedDateTime modifiedTime;

  public String getEmail() {
    return email.toLowerCase();
  }
  public void setEmail(String email) {
    this.email = email.toLowerCase();
  }
}
