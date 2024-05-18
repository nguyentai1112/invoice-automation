package com.example.service;

import com.example.config.Oauth2GmailConfig;
import com.example.dto.Attachment;
import com.example.dto.Email;
import com.example.dto.EmailOutBox;
import com.example.util.EmailUtil;
import com.example.util.JsonUtil;
import com.example.util.StringUtil;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class GmailApiService {

  private static final String USER = "me";
  private static final Long MAX_RESULTS = 100000l;

  private final Gmail gmailService;

  public List<EmailOutBox> getEmailMessageInfo(ZonedDateTime fromDate, ZonedDateTime toDate)
      throws IOException {

    List<Message> messages = listMessagesInTimeRange("me", fromDate, toDate);
    if(messages == null || messages.isEmpty()) {
      log.info("No messages found during the time range: {} - {}", fromDate, toDate);
      return Collections.emptyList();
    }
    List<EmailOutBox> outBoxes = new ArrayList<>();
    // Handle retrieved messages as required
    for (Message message : messages) {
      try {
        String messageId = message.getId();
        // if(messageId.equalsIgnoreCase("18cbeb967e8ec40e")){
        // log.info("debug: {}", messageId);
        //}
        Message fullMessage = getMessage("me", messageId);

        // Extract subject and body
        String subject = "";
        String body = "";
        String sender = null;
        String receiver = null;
        String sentDate = null;
        String cc = null;
        List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
        for (MessagePartHeader header : headers) {
          if (header.getName().equals("Subject")) {
            subject = header.getValue();
          }
          if (header.getName().equalsIgnoreCase("From")) {
            sender = header.getValue();
          }
          if (header.getName().equalsIgnoreCase("To")) {
            receiver = header.getValue();
          }
          if (header.getName().equalsIgnoreCase("Date")) {
            sentDate = header.getValue();
          }
          if (header.getName().equalsIgnoreCase("Cc")) {
            cc = header.getValue();
          }

        }
        body = fullMessage.getSnippet(); // Getting the snippet as an example of the body

        //if the sender is an email list, we will get the first email
        //the others will move to cc
        List<Email> receivers = EmailUtil.extractEmailList(receiver);
        final String originalReceiver = receiver;

        if (receivers.isEmpty()) { //there is no to email.
          receivers.add(EmailUtil.extractEmail(sender));
        }

        if (receivers.size() > 1) {
          //join the rest of emails to a string with comma separator
          cc = EmailUtil.addEmailsToCc(receivers.subList(1, receivers.size()), cc);
        }

        EmailOutBox outBox = EmailOutBox.builder()
            .messageId(messageId)
            .title(subject).body(body)
            .sender(EmailUtil.extractEmail(sender))
            .originalRawReceiver(originalReceiver)
            .receiver(receivers.get(0))
            .cc(cc)
            .sentTime(StringUtil.convertGmailTimeToZoneDateTime(sentDate))
            .internalDate(fullMessage.getInternalDate())
            .threadId(fullMessage.getThreadId())
            .body(body)
            .build();

        //  extractAndSavePdfAttachments(outBox, fullMessage.getPayload().getParts());
        //get attachment infos
        List<Attachment> attachments = new ArrayList<>();
        for (MessagePart part : fullMessage.getPayload().getParts()) {
          if (part.getFilename() != null && part.getFilename().endsWith(".pdf")) {
            // Fetch attachment content using attachmentId
            byte[] fileByteArray = gmailService.users().messages().attachments()
                .get(USER, outBox.getMessageId(), part.getBody().getAttachmentId())
                .execute()
                .decodeData();
            attachments.add(Attachment.builder()
                .attachmentId(part.getBody().getAttachmentId())
                .data(fileByteArray)
                .fileName(part.getFilename())
                .build());
          }

        }
        outBox.setAttachments(attachments);
        log.info("MessageId: {}, Received email: {}", messageId, JsonUtil.toJson(outBox));

        outBoxes.add(outBox);
      }
      catch (Exception e) {
        log.error("Error when processing message: {}", message.getId(), e);
      }
    }
    return outBoxes;
  }

  public static void main(String[] args) throws GeneralSecurityException, IOException {
    GmailApiService gmailApiService = new GmailApiService(
        new Oauth2GmailConfig().oauth2GmailService());

    // Define the time range (adjust the time accordingly)
    ZonedDateTime startTime = ZonedDateTime.parse("2024-01-09T00:00:00Z",
        DateTimeFormatter.ISO_DATE_TIME);
    ZonedDateTime endTime = ZonedDateTime.parse("2024-02-01T00:00:00Z",
        DateTimeFormatter.ISO_DATE_TIME);

    gmailApiService.getEmailMessageInfo(startTime, endTime);

  }


  private List<Message> listMessagesInTimeRange(String userId,
      ZonedDateTime startTime, ZonedDateTime endTime) throws IOException {
    //get data >= startTime and <= endTime
    String query = "after:" + (startTime.toEpochSecond()) + " before:" + (endTime.toEpochSecond() + 1);
    ListMessagesResponse response = gmailService.users().messages().list(userId).setQ(query)
        .setLabelIds(Collections.singletonList("SENT")).setMaxResults(MAX_RESULTS).execute();

    return response.getMessages();
  }

  private Message getMessage(String userId, String messageId)
      throws IOException {
    return gmailService.users().messages().get(userId, messageId).execute();
  }

}