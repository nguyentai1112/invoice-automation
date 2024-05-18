package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class Attachment {
  private String attachmentId;
  private byte[] data;
  private String fileName;
  private String savedPath;

  @JsonIgnore
  public byte[] getData() {
    return data;
  }

}
