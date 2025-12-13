
const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

  //  FILE UPLOAD 

export async function uploadFile(file) {
  const form = new FormData();
  form.append("file", file);

  const resp = await fetch(`${API_BASE}/files/upload`, {
    method: "POST",
    body: form,
  });

  if (!resp.ok) {
    const err = await resp.text();
    throw new Error("Upload failed: " + err);
  }

  return resp.json(); // returns DocInfo { docId, filename, preview }
}


  //  ASK QUESTION (Groq + Chunks)

export async function askQuestion(question, docId) {
  const resp = await fetch(`${API_BASE}/ask`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      question,
      docId: docId || ""  
    }),
  });

  return resp.json();
}


  //  DOCUMENT SUMMARIZATION

export async function summarizeDoc(docId) {
  const resp = await fetch(`${API_BASE}/summarize/${docId}`);
  return resp.json();
}

  //  QUIZ GENERATION

export async function generateQuiz(docId) {
  const resp = await fetch(`${API_BASE}/quiz/${docId}`);
  return resp.json();
}

