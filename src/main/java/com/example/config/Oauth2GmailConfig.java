package com.example.config;

import com.example.service.GmailApiService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
@Slf4j
public class Oauth2GmailConfig {

  private static final String APPLICATION_NAME = "My Spring Boot App";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String CREDENTIALS_FILE_PATH = "credentials.json";
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);

  private static String LOGIN_URL = "https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=657372073293-t8t53uef8eeo4un5tips72k4uj9ih0lq.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/gmail.readonly&promt=login";

  @Bean
  public Gmail oauth2GmailService() throws IOException, GeneralSecurityException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    Credential credential = null;
    try {
      credential = getCredentials(httpTransport);
    } catch (Exception e) {
      log.info("Please log in to your Google account to authorize the application");

      // Open the authorization URL in a web browser
      openBrowser(LOGIN_URL);

    }
    return new Gmail.Builder(httpTransport, JSON_FACTORY,
        credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = new FileInputStream(ResourceUtils.getFile(CREDENTIALS_FILE_PATH));
    GmailApiService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(
            new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
       // .setApprovalPrompt("force")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;

  }

  private static void openBrowser(String url) {
    try {
      // Get the desktop instance
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Desktop.Action.BROWSE)) {
        // Open the URL in the default browser
        desktop.browse(new URI(url));
      } else {
        System.out.println("Desktop browsing is not supported.");
      }
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
