package com.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SqlService {

    // Inject DataSource or JdbcTemplate here
    private final JdbcTemplate jdbcTemplate;

    public SqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> executeSqlQueries(List<String> sqlQueries) {
        List<String> results = new ArrayList<>();
        for (String sql : sqlQueries) {
            try {
                jdbcTemplate.execute(sql);
                results.add("Query executed successfully: " + sql);
            } catch (Exception e) {
                results.add("Error executing query (" + sql + "): " + e.getMessage());
            }
        }
        return results;
    }
}