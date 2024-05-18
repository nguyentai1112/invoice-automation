package com.example.util;

import com.example.exception.InternalException;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class StringUtil {

  public static final String SLASH = File.separator;
  public static final String SPACE = " ";
  private static final ZoneId FIXED_ZONE_ID = ZoneId.systemDefault();
  public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyyMMdd_HHmmss");
  private static DateTimeFormatter GMAIL_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "EEE, d MMM yyyy HH:mm:ss Z");
  private static DateTimeFormatter SAVED_FOLDER_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy_MM_dd");

  private static DateTimeFormatter DISPLAY_DATETIME_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");



  public static long stringNumToLong(String stringNum) {
    final String cleanStringNum = stringNum.replace(".", "").replace(",", "").trim();
    return Long.parseLong(cleanStringNum);
  }


  public static int stringPercentToInt(String stringNum) {
    final String cleanStringNum = stringNum.replace(".", "").replace("%", "").trim();
    return Integer.parseInt(cleanStringNum);
  }




  public static String extractFromPattern(String text, String patternString, int groupIndex)
      throws InternalException {
    return extractFromPattern(text, patternString).group(groupIndex);
  }

  public static Matcher extractFromPattern(String text, String patternString)
      throws InternalException {

    Pattern pattern = Pattern.compile(patternString);

    // Matcher to find the pattern in the text
    Matcher matcher = pattern.matcher(text);

    // Find and extract the number
    if (matcher.find()) {
      return matcher;
    }
    throw new InternalException("Pattern not found: " + patternString);
  }

  public static String removeSpaces(String string) {
    return string.replaceAll("\\s+", "");
  }

  public static String replaceSpacesWithUnderScores(String string) {
    return string.replaceAll("\\s+", "_");
  }

  public static String generateFileName(String email) {
    Instant instant = Instant.now();
    LocalDateTime currentTime = instant.atZone(FIXED_ZONE_ID).toLocalDateTime();
    //  LocalDateTime currentTime = LocalDateTime.now();
    String formattedTime = currentTime.format(DATETIME_FORMATTER);

    String fileName = email + "_" + formattedTime;
    return fileName;
  }

  //input  "Wed, 10 Jan 2024 17:13:31 +0700";
  public static ZonedDateTime convertGmailTimeToZoneDateTime(String timeString) {
    // Parse the Gmail time string to ZonedDateTime
    return ZonedDateTime.parse(timeString, GMAIL_TIME_FORMATTER);

  }

  public static String generateFolderFromDate(ZonedDateTime sentTime) {
    return SAVED_FOLDER_FORMATTER.format(sentTime);
  }

  public static String paddingZero(String input) {
    if (input.length() == 1) {
      // Add a leading zero
      input = "0" + input;
    }
    return input;
  }

  public static String displayDateTime(ZonedDateTime zonedDateTime) {
    return zonedDateTime.format(DISPLAY_DATETIME_FORMATER);
  }

    public static String normalizePath(String path) {
      // Replace all backslashes with forward slashes
      String standardizedPath = path.replace("\\", "/");

      // Get the correct separator for the operating system
      String osSeparator = File.separator;

      // Replace forward slashes with the OS-specific separator
      String normalizedPath = standardizedPath.replace("/", osSeparator);

      return normalizedPath;

  }
}
