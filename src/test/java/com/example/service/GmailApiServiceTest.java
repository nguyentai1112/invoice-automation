package com.example.service;

import com.example.config.Oauth2GmailConfig;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class GmailApiServiceTest {

  GmailApiService gmailApiService;

  @BeforeEach
  void setUp() throws GeneralSecurityException, IOException {
    gmailApiService = new GmailApiService(
        new Oauth2GmailConfig().oauth2GmailService());

  }

  @Test
  void process() throws IOException {

    // Define the time range (adjust the time accordingly)
    ZonedDateTime startTime = ZonedDateTime.parse("2023-12-31T05:55:15+07:00",
        DateTimeFormatter.ISO_DATE_TIME);
    ZonedDateTime endTime = ZonedDateTime.parse("2023-12-31T23:59:59+07:00",
        DateTimeFormatter.ISO_DATE_TIME);

    gmailApiService.getEmailMessageInfo(startTime, endTime);
  }

  @Test
  void writeToFile() {
  }
}