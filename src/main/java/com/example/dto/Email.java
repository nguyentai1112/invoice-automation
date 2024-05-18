package com.example.dto;

import com.example.util.EmailUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email {
  String email;
  String name;

  @Override
  public String toString() {
    if(name == null || name.isBlank() || name.equals(EmailUtil.NO_NAME)){
      return email;
    }
    return name + " <" + email + ">";
  }
}
