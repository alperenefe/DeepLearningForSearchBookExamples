package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SimpleSearch {
    public static void main(String[] args) throws IOException, ParseException {

        Path path = Paths.get("./luceneidx");
        try {
            Directory directory = FSDirectory.open(path);

            IndexWriter writer = createWriter(directory);
            createDocuments(writer);
            Query query = createQuery("title", "search");
            search(directory, query);
        } finally {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static void search(Directory directory, Query query) throws IOException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs hits = searcher.search(query, 10);

        System.out.println("hits length:" + hits.scoreDocs.length);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = reader.document(scoreDoc.doc);
            System.out.println(doc.get("title") + " : " + scoreDoc.score);
        }
    }

    private static Query createQuery(String fieldName, String queryString) throws ParseException {
        QueryParser parser = new QueryParser(fieldName,
                new WhitespaceAnalyzer());
        Query query = parser.parse(queryString);
        return query;
    }

    static IndexWriter createWriter(Directory directory) throws IOException {
        Analyzer analyzer = createIndexAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, config);
    }

    static Analyzer createIndexAnalyzer() {
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        CharArraySet stopWords = new CharArraySet(Arrays
                .asList("a", "an", "the"), true);
        perFieldAnalyzers.put("pages", new StopAnalyzer(
                stopWords));
        perFieldAnalyzers.put("title", new WhitespaceAnalyzer());
        Analyzer analyzer = new PerFieldAnalyzerWrapper(
                new EnglishAnalyzer(), perFieldAnalyzers);
        return analyzer;
    }

    static void createDocuments(IndexWriter writer) throws IOException {

        Document dl4s = new Document();
        dl4s.add(new TextField("title", "DL for search", Field.Store.YES));
        dl4s.add(new TextField("page", "Living in the information age ...", Field.Store.YES));

        Document rs = new Document();
        rs.add(new TextField("title", "Relevant search", Field.Store.YES));
        rs.add(new TextField("page", "Getting a search engine to behave ...", Field.Store.YES));

        writer.addDocument(dl4s);
        writer.addDocument(rs);

        writer.commit();
        writer.close();
    }
}
