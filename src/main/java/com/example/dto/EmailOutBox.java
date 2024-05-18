package com.example.dto;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailOutBox {

  private String messageId;
  private String title;
  private ZonedDateTime sentTime;
  private String body;
  private Email sender;
  private String originalRawReceiver;
  private Email receiver;
  private String cc;
  private Long internalDate;
  private String threadId;
  private List<Attachment> attachments;

}
