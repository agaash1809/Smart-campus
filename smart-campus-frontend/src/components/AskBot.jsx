import React, { useState } from "react";
import { askQuestion } from "../services/api";

export default function AskBox() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleAsk() {
    if (!question.trim()) return;
    setLoading(true);
    setAnswer("");

    try {
      const res = await askQuestion(question);
      setAnswer(res.answer);
    } catch (err) {
      setAnswer("Error fetching answer.");
    }

    setLoading(false);
  }

  return (
    <div>
      <input
        value={question}
        onChange={(e) => setQuestion(e.target.value)}
        placeholder="Ask a question..."
        style={{
          width: "300px",
          padding: "10px",
          marginRight: "8px"
        }}
      />

      <button onClick={handleAsk} style={{ padding: "10px" }}>
        Ask
      </button>

      <div style={{ marginTop: "20px", whiteSpace: "pre-wrap" }}>
        {loading ? "Thinking..." : answer}
      </div>
    </div>
  );
}
