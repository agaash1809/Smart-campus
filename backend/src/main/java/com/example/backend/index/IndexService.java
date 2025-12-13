package com.example.backend.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndexService implements DisposableBean {

    private static final String FIELD_DOCID = "docId";
    private static final String FIELD_CHUNKID = "chunkId";
    private static final String FIELD_CONTENT = "content";

    private final Path indexPath = Path.of("D:/SMART CAMPUS PROJECT/uploads/index");

    private IndexWriter writer;
    private Analyzer analyzer;

    @PostConstruct
    public void init() throws IOException {
        System.out.println("Initializing Lucene Index Service...");

        analyzer = new StandardAnalyzer();

        Files.createDirectories(indexPath);
        System.out.println("Index directory ensured at: " + indexPath.toString());

        FSDirectory dir = FSDirectory.open(indexPath);

        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        writer = new IndexWriter(dir, cfg);

        System.out.println("Lucene IndexWriter initialized successfully!");
    }

   public void indexDocument(String docId, String text) throws IOException {
    System.out.println("INDEXING STARTED for doc " + docId);
    writer.deleteDocuments(new Term("docId", docId));
    System.out.println("OLD INDEX REMOVED");

    int chunkSize = 500;
    int overlap = 100;

    int start = 0;
    int chunkNum = 0;

    while (start < text.length()) {

        int end = Math.min(start + chunkSize, text.length());
        String chunk = text.substring(start, end);

        Document doc = new Document();
        doc.add(new StringField("docId", docId, Field.Store.YES));
        doc.add(new StringField("chunkId", docId + "_" + chunkNum, Field.Store.YES));
        doc.add(new TextField("content", chunk, Field.Store.YES));

        writer.addDocument(doc);
        System.out.println("CHUNK ADDED: " + chunkNum);

        chunkNum++;

        start = start + (chunkSize - overlap);

     
        if (start <= 0) break;
    }

    writer.commit();
    System.out.println("INDEX COMMIT DONE");
}


    public List<SearchResult> search(String queryString, String docIdFilter, int topK) throws Exception {
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            String[] fields = new String[]{FIELD_CONTENT};
            QueryParser parser = new MultiFieldQueryParser(fields, analyzer);

            Query query = parser.parse(QueryParser.escape(queryString));

            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            bq.add(query, BooleanClause.Occur.MUST);

            if (docIdFilter != null && !docIdFilter.isBlank()) {
                Query docFilter = new TermQuery(new Term(FIELD_DOCID, docIdFilter));
                bq.add(docFilter, BooleanClause.Occur.FILTER);
            }

            TopDocs docs = searcher.search(bq.build(), topK);

            List<SearchResult> results = new ArrayList<>();
            for (ScoreDoc sd : docs.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                results.add(new SearchResult(
                        d.get(FIELD_DOCID),
                        d.get(FIELD_CHUNKID),
                        d.get(FIELD_CONTENT),
                        sd.score
                ));
            }
            return results;
        }
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Closing Lucene writer...");
        if (writer != null) writer.close();
        if (analyzer != null) analyzer.close();
    }

    public static class SearchResult {
        public final String docId;
        public final String chunkId;
        public final String content;
        public final float score;

        public SearchResult(String docId, String chunkId, String content, float score) {
            this.docId = docId;
            this.chunkId = chunkId;
            this.content = content;
            this.score = score;
        }
    }
}