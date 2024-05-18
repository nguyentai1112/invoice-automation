package com.example.controller;

import com.example.util.StringUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {
  private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");


  @GetMapping("/{filename:.+}")
  public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename,
      @RequestParam("path") String path) {
    try {
      Path filePath = Paths.get(CURRENT_DIRECTORY + StringUtil.SLASH + path);
      Resource resource = new UrlResource(filePath.toUri());
      System.out.println("resource: " + filePath.toString());
      if (resource.exists()) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (MalformedURLException e) {
      // Handle the exception appropriately
      return ResponseEntity.badRequest().build();
    }
  }
}

