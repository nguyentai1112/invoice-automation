package com.example.util;

import static org.junit.jupiter.api.Assertions.*;

import com.example.dto.Email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class EmailUtilTest {

  EmailUtil emailUtil;

  @BeforeEach
  void setUp() {
    EmailUtil emailUtil = new EmailUtil();

  }

  @ParameterizedTest
  @CsvSource(value = {"thientai nguyen <quachthanhtrung@gmail.com>,quachthanhtrung@gmail.com",
      "Tai Nguyen <nguyentai1112@gmail.com>,nguyentai1112@gmail.com"}, delimiter = ',')
  void extractEmails(String input, String expected) {
    Email email = emailUtil.extractEmail(input);
    assertEquals(expected, email.getEmail());
  }

  @ParameterizedTest
  @MethodSource("generateData")
  void extractEmailList(String input, List<Email> expectedEmails) {
    List<Email> emails = emailUtil.extractEmailList(input);
    assertEquals(emails.get(0).getEmail(), expectedEmails.get(0).getEmail());
    assertEquals(emails.get(0).getName(), expectedEmails.get(0).getName());
    assertEquals(emails.get(1).getName(), expectedEmails.get(1).getName());
    assertEquals(emails.get(1).getEmail(), expectedEmails.get(1).getEmail());
  }

  static Stream<Arguments> generateData() {
    return Stream.of(
        Arguments.of("thientai nguyen <quachthanhtrung@gmail.com>,quachthanhtrung@gmail.com",
            Arrays.asList(Email.builder().email("quachthanhtrung@gmail.com").name("thientai nguyen").build(),
                Email.builder().email("quachthanhtrung@gmail.com").name("NO_NAME").build()))
    );
  }

  @Test
  void testExtractEmailList() {
  }

  @Test
  void addEmailsToCc() {
    // Create a list of emails
    List<Email> emails = new ArrayList();
    emails.add(new Email("quachthanhtrungltv@gmail", "NO_NAME"));
    emails.add(new Email("tuongminh11@gmail", "Tuong Minh"));

    // Set initial cc value
    String cc = "initial_cc@gmail.com";

    // Call the addEmailsToCc function
    String result = EmailUtil.addEmailsToCc(emails, cc);

    // Assert the result
    assertEquals("quachthanhtrungltv@gmail,Tuong Minh <tuongminh11@gmail>,initial_cc@gmail.com", result);
  }
}