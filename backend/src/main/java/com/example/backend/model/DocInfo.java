package com.example.backend.model;


public class DocInfo {
    private String docId;
    private String fileName;
    private String textPreview;

   
    public DocInfo() {}
    public DocInfo(String docId, String fileName, String textPreview){
        this.docId = docId; this.fileName = fileName; this.textPreview = textPreview;
    }


    public String getDocId() { return docId; }
public void setDocId(String docId) { this.docId = docId; }

public String getFileName() { return fileName; }
public void setFileName(String fileName) { this.fileName = fileName; }

public String getTextPreview() { return textPreview; }
public void setTextPreview(String textPreview) { this.textPreview = textPreview; }

}
