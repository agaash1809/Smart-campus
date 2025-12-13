package com.example.backend.service;

import com.example.backend.index.IndexService;
import com.example.backend.model.DocInfo;
import com.example.backend.store.DocStore;
import com.example.backend.util.Extractors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private IndexService indexService;

    public DocInfo saveAndExtract(MultipartFile multipart) throws Exception {
        System.out.println("UPLOAD STARTED: " + multipart.getOriginalFilename());

        Files.createDirectories(Path.of(uploadDir));
        System.out.println("DIRECTORY OK");

        String original = multipart.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }

        String id = UUID.randomUUID().toString();
        String fileName = id + ext;

        Path target = Path.of(uploadDir, fileName);
        System.out.println("TARGET PATH: " + target.toString());

        multipart.transferTo(target.toFile());
        System.out.println("FILE SAVED");

        File f = target.toFile();

        String text = "";
        System.out.println("EXTRACTION START: " + ext);

        switch (ext) {
            case ".pdf":
                text = Extractors.extractFromPDF(f);
                break;
            case ".docx":
                text = Extractors.extractFromDocx(f);
                break;
            case ".pptx":
                text = Extractors.extractFromPptx(f);
                break;
            default:
                System.out.println("UNSUPPORTED FORMAT");
                text = "Unsupported file type: " + ext;
        }

        System.out.println("EXTRACTION DONE");

        
        DocStore.DOC_TEXTS.put(id, text);
        System.out.println("TEXT STORED. LENGTH = " + text.length());

       
        indexService.indexDocument(id, text);
        System.out.println("INDEXING COMPLETE");

        String preview = text.length() > 800 ? text.substring(0, 800) + "..." : text;
        return new DocInfo(id, original, preview);
    }
}