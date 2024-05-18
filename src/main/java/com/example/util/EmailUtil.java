package com.example.util;

import com.example.dto.Email;
import java.util.ArrayList;
import java.util.List;

public class EmailUtil {
  public static final String NO_NAME = "NO_NAME";

  //input:  Tuong Minh <tuongminh11@gmail.com>
  //output: Email (tuongminh11@gmail, Tuong Minh)
  public static Email extractEmail(String text) {
    Email email = new Email();
    int start = text.indexOf("<");
    int end = text.indexOf(">");
    if (start == -1 || end == -1) {
      email.setEmail(text.trim());
      email.setName(NO_NAME);
    } else {
      email.setEmail(text.substring(start + 1, end).trim());
      email.setName(text.substring(0, start).trim());
    }
    return email;
  }

  //input:  quachthanhtrungltv@gmail.com, Tuong Minh <tuongminh11@gmail.com>
  //output: List.of(Email (tuongminh11@gmail, Tuong Minh), Email (quachthanhtrungltv@gmail, NO_NAME))
  public static List<Email> extractEmailList(String cc) {
    if(cc == null || cc.isBlank()){
      return new ArrayList<>();
    }
    List emails = new ArrayList();
    String[] parts = cc.split(",");
    for (String part : parts) {
      Email email = extractEmail(part);
      emails.add(email);

    }
    return emails;

  }


  public static String addEmailsToCc(List<Email> emails, String cc) {
    String emailsString = "";
    for (int i = 0; i < emails.size(); i++) {
      Email email = emails.get(i);
      if(i == emails.size() - 1){
        emailsString += email.toString();
      }else {
        emailsString += email.toString() + ",";
      }
    }

    return cc == null? emailsString: emailsString + "," + cc;

  }
}
