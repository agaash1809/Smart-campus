
import React, { useState, useEffect } from "react";
import { generateQuiz } from "../services/api";


export default function QuizView({ docId }) {
  const [quiz, setQuiz] = useState([]);
  const [results, setResults] = useState({});
  const [loading, setLoading] = useState(false);
  const [showScore, setShowScore] = useState(false); 

  useEffect(() => {
  if (quiz.length === 10 && Object.keys(results).length === 10) {
    setShowScore(true); 
  }
}, [results, quiz]);


  async function fetchQuiz() {
    setLoading(true);
    setQuiz([]);
    setResults({});
    setShowScore(false); 
    const res = await generateQuiz(docId);

    try {
      const parsed = JSON.parse(res.quiz); 
      setQuiz(parsed.quiz || []);
    } catch (e) {
      console.error("JSON Parse error", e, res.quiz);
      alert("Invalid quiz format from backend. Fix prompt.");
    }

    setLoading(false);
  }

  function handleAnswer(qIndex, selected) {
    if (results[qIndex]) return; 

    const correct = quiz[qIndex].answer;

    setResults(prev => ({
      ...prev,
      [qIndex]: { selected, correct }
    }));
  }

  //  score calculation
  const totalAnswered = Object.keys(results).length;
  const score = Object.values(results).filter(
    r => r.selected === r.correct
  ).length;

  return (
    <div>
      <button onClick={fetchQuiz} disabled={!docId || loading}>
        {loading ? "Generating..." : "Generate Quiz"}
      </button>

      {quiz.map((q, qi) => (
        <div
          key={qi}
          style={{
            marginTop: 16,
            border: "1px solid #444",
            padding: 12,
            borderRadius: 8
          }}
        >
          <b>{qi + 1}. {q.q}</b>

          {q.options.map((opt, oi) => (
          <div
  key={oi}
  onClick={() => handleAnswer(qi, oi)}
  style={{
    marginTop: 8,
    padding: 10,
    border: "1px solid #444242ff",
    borderRadius: 6,
    background: "#222",
    cursor: results[qi] ? "not-allowed" : "pointer",

    /*  Highlight selected option */
    transform:
      results[qi]?.selected === oi ? "scale(1.02)" : "scale(1)",
    borderColor:
      results[qi]?.selected === oi ? "#fff" : "#666",
    boxShadow:
      results[qi]?.selected === oi
        ? "0 0 8px rgba(157, 34, 34, 0.6)"
        : "none",

    transition: "all 0.2s ease-in-out"
  }}
>
  {["A","B","C","D"][oi]}. {opt}
</div>

          ))}

          {results[qi] && (
            <div style={{ marginTop: 10, fontWeight: "bold" }}>
              {results[qi].selected === results[qi].correct ? (
                <span style={{ color: "green" }}>✔ Correct Answer</span>
              ) : (
                <span style={{ color: "red" }}>
                  ✘ Wrong Answer — Correct Answer:{" "}
                  {["A","B","C","D"][results[qi].correct]} (
                  {q.options[results[qi].correct]})
                </span>
              )}
            </div>
          )}
        </div>
      ))}

      {/* View button score */}
      {quiz.length === 10 && totalAnswered === 10 && (
        <button
          onClick={() => setShowScore(true)}
          style={{
            marginTop: 20,
            padding: "10px 16px",
            borderRadius: 6,
            border: "1px solid #444",
            cursor: "pointer"
          }}
        >
          View Score
        </button>
      )}

      {/*  SCORE POPUP */}
      {showScore && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0,0,0,0.6)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 999
          }}
        >
          <div
            style={{
              background: "#111",
              border: "2px solid #444",
              borderRadius: 10,
              padding: 24,
              width: 300,
              textAlign: "center"
            }}
          >
            <h2>Quiz Result</h2>
            <p style={{ fontSize: 18, marginTop: 10 }}>
              Score: <b>{score} / 10</b>
            </p>

            <button
              onClick={() => setShowScore(false)}
              style={{
                marginTop: 20,
                padding: "8px 14px",
                borderRadius: 6,
                border: "1px solid #444",
                cursor: "pointer"
              }}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
