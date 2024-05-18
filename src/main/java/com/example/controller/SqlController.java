package com.example.controller;

import com.example.dto.SqlRequest;
import com.example.service.SqlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sql")
public class SqlController {

    private final SqlService sqlService;

    public SqlController(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    @PostMapping("/execute")
    public ResponseEntity<List<String>> executeSql(@RequestBody SqlRequest sqlRequest) {
        try {
            List<String> results = sqlService.executeSqlQueries(sqlRequest.getSqlQueries());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Error executing SQL queries: " + e.getMessage()));
        }
    }
}