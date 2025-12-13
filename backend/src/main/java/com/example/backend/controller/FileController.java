package com.example.backend.controller;



import com.example.backend.index.IndexService;
import com.example.backend.model.DocInfo;
import com.example.backend.service.FileService;
import com.example.backend.store.DocStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private IndexService indexService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            DocInfo info = fileService.saveAndExtract(file);

            
            String fullText = DocStore.DOC_TEXTS.get(info.getDocId());
            indexService.indexDocument(info.getDocId(), fullText);

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}