import React, { useState, useRef, useEffect } from "react";
import { askQuestion } from "../services/api";

export default function ChatBox({ docId }) {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([]);

  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function send() {
    if (!input.trim() || !docId) return;

    const userMsg = { role: "user", text: input };
    setMessages((prev) => [...prev, userMsg]);

    const res = await askQuestion(input, docId);
    const botMsg = { role: "assistant", text: res.answer || "Error!" };

    setMessages((prev) => [...prev, botMsg]);
    setInput("");
  }

  return (
    <div
      style={{
        border: "1px solid #444",
        borderRadius: 8,
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* SCROLLABLE MESSAGE AREA */}
      <div
        style={{
          flex: 1,
          overflowY: "auto",
          padding: 12,
        }}
      >
        {messages.map((m, i) => (
          <div key={i} style={{ margin: "8px 0" }}>
            <b>{m.role === "user" ? "You" : "Jarvis"}:</b> {m.text}
          </div>
        ))}

        {/* AUTO-SCROLL TARGET */}
        <div ref={bottomRef} />
      </div>

      {/* FIXED INPUT BAR */}
      <div
        style={{
          padding: 10,
          // borderTop: "1px solid #444",
          display: "flex",
          gap: 8,
          // background: "#1a1a1a", 
        }}
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={docId ? "Ask a question..." : "Upload a document first"}
          style={{
            flex: 1,
            padding: "8px",
            borderRadius: 6,
            border: "1px solid #555",
            background: "#222",
            color: "white",
          }}
        />
        <button onClick={send}>Ask</button>
      </div>
    </div>
  );
}
