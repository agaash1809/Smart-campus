// src/components/FileUpload.jsx
import React, { useState, useRef } from "react";
import { uploadFile } from "../services/api";

export default function FileUpload({ onUploaded }) {
  const [dragActive, setDragActive] = useState(false);
  const [previewText, setPreviewText] = useState(null);
  const inputRef = useRef();

  async function handleFiles(files) {
    const file = files[0];
    if (!file) return;
    setPreviewText(`Uploading ${file.name} ...`);
    try {
      const res = await uploadFile(file);
      setPreviewText(`Uploaded: ${res.fileName}`);
      if (onUploaded) onUploaded(res);
    } catch (e) {
      setPreviewText("Upload failed");
    }
  }

  function onDrag(e) {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") setDragActive(true);
    else if (e.type === "dragleave") setDragActive(false);
  }

  return (
    <div>
      <div
        onDragEnter={onDrag}
        onDragOver={onDrag}
        onDragLeave={onDrag}
        onDrop={(e) => {
          onDrag(e);
          handleFiles(e.dataTransfer.files);
        }}
        style={{
          border: "2px dashed #aaa",
          padding: 24,
          borderRadius: 8,
          textAlign: "center",
          background: dragActive ? "#635f5fff" : "#333232ff",
        }}
      >
        <p>Drag & drop a file here (PDF, DOCX, PPTX) or</p>
        <button onClick={() => inputRef.current.click()}>Select file</button>
        <input
          ref={inputRef}
          type="file"
          style={{ display: "none" }}
          onChange={(e) => handleFiles(e.target.files)}
        />
      </div>

      {previewText && <div style={{ marginTop: 12 }}>{previewText}</div>}
    </div>
  );
}
