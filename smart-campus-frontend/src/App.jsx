import React, { useState } from "react";
import FileUpload from "./components/FileUpload";
import ChatBox from "./components/ChatBox";
import SummaryView from "./components/SummaryView";
import QuizView from "./components/QuizView";

export default function App() {
  const [activeDoc, setActiveDoc] = useState(null);
  const [rightMode, setRightMode] = useState(null); // "summary" | "quiz"

  return (
    <div
      style={{
        height: "100vh",
        width: "100vw",
        overflow: "hidden",
        border: "2px solid #333",
        boxSizing: "border-box",
        paddingBottom: 10,
       
      }}
    >
      <h1 style={{ padding: 20, margin:0}}>Smart Campus — Jarvis</h1>

      {/* TWO EQUAL COLUMNS */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          height: "calc(100vh - 100px)",   // FIXED HEIGHT (prevents overflow)
          gap: 20,
          padding: "0 20px 20px 20px",
          boxSizing: "border-box",
        }}
      >

        {/* LEFT PANEL */}
        <div
          style={{
            padding: 20,
            border: "2px solid #333",
            borderRadius: 8,
            height: "100%",
            minHeight: 0,                // IMPORTANT FIX
            display: "flex",
            flexDirection: "column",
            gap: 20,
          
          }}
        >
          <FileUpload onUploaded={(res) => setActiveDoc(res)} />

          <div style={{ flex: 1, minHeight: 0 }}>
            <ChatBox docId={activeDoc?.docId} />
          </div>
        </div>

        {/* RIGHT PANEL */}
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            padding: 20,
            border: "2px solid #333",
            borderRadius: 8,
            height: "100%",
            minHeight: 0,                // IMPORTANT FIX
            overflow: "hidden",
           
          }}
        >
          <h2 style={{ marginTop: 0 }}>Document Details</h2>
          <p>
            Selected: <b>{activeDoc?.fileName || "None"}</b>
          </p>

          {/* TOP BUTTONS */}
          <div
            style={{
              display: "flex",
              gap: 10,
              marginBottom: 15,
            }}
          >
            <button
              onClick={() => setRightMode("summary")}
              disabled={!activeDoc}
              style={{
                flex: 1,
                padding: "10px",
                borderRadius: 6,
                border: "1px solid #444",
                cursor: activeDoc ? "pointer" : "not-allowed",
              }}
            >
              Summary
            </button>

            <button
              onClick={() => setRightMode("quiz")}
              disabled={!activeDoc}
              style={{
                flex: 1,
                padding: "10px",
                borderRadius: 6,
                border: "1px solid #444",
                cursor: activeDoc ? "pointer" : "not-allowed",
              }}
            >
              Quiz
            </button>
          </div>

          {/* MAIN CONTENT AREA */}
          <div
            style={{
              flex: 1,
              minHeight: 0,
              overflowY: "auto",
              border: "1px solid #444",
              borderRadius: 8,
              padding: 10,
            }}
          >
            {!rightMode && <p>Select Summary or Quiz to continue...</p>}

            {rightMode === "summary" && activeDoc?.docId && (
              <SummaryView docId={activeDoc.docId} autoRun={true} />
            )}

            {rightMode === "quiz" && activeDoc?.docId && (
              <QuizView docId={activeDoc.docId} autoRun={true} />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
