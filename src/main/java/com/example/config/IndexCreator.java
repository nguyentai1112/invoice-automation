package com.example.config;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IndexCreator {

  private final DataSource dataSource;

  //Sqlite does not support create unique index if not exists,
  // so we have to create it manually
  @PostConstruct
  public void createUniqueIndex() {
    try (Connection connection = dataSource.getConnection()) {
      Statement statement = connection.createStatement();

      String sql = "CREATE UNIQUE INDEX IF NOT EXISTS unique_invoice_index ON invoice_entity(invoice_date, invoice_no, seller_tax_code)";
      statement.executeUpdate(sql);

      System.out.println("Unique index created successfully.");
    } catch (Exception e) {
      System.err.println("Error creating unique index: " + e.getMessage());
      e.printStackTrace();
    }
  }
}