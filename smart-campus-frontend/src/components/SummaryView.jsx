import React, { useState, useEffect } from "react";
import { summarizeDoc } from "../services/api";

export default function SummaryView({ docId, autoRun }) {
  const [summary, setSummary] = useState("Loading summary...");
  
  useEffect(() => {
    if (autoRun && docId) {
      summarizeDoc(docId).then((res) => {
        setSummary(res.summary || "No summary available.");
      });
    }
  }, [docId, autoRun]);

  return <div>{summary}</div>;
}
