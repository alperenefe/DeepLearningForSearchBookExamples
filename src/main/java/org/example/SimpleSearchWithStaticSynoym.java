package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
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
import org.apache.lucene.util.CharsRef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class SimpleSearchWithStaticSynoym {
    public static void main(String[] args) throws IOException, ParseException {

        Path path = Paths.get("./luceneidx");
        try {
            Directory directory = FSDirectory.open(path);

            IndexWriter writer = createWriter(directory);
            createDocuments(writer);
            Query query = createQuery("title", "plane");
            search(directory, query);
        } finally {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static Analyzer createIndexAnalyzer() throws IOException {
        SynonymMap.Builder builder = new SynonymMap.Builder();
        builder.add(new CharsRef("aeroplane"), new CharsRef("plane"), true);
        final SynonymMap map = builder.build();



        Analyzer indexTimeAnalyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(
                    String fieldName) {
                Tokenizer tokenizer = new WhitespaceTokenizer();
                SynonymGraphFilter synFilter = new
                        SynonymGraphFilter(tokenizer, map, true);
                return new TokenStreamComponents(tokenizer, synFilter);
            }
        };

        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        Analyzer analyzer = new PerFieldAnalyzerWrapper(
                indexTimeAnalyzer, perFieldAnalyzers);
        return analyzer;
    }

    private static void search(Directory directory, Query query) throws IOException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs hits = searcher.search(query, 1);

        System.out.println("hits length:" + hits.scoreDocs.length);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = reader.document(scoreDoc.doc);
            System.out.println(doc.get("title") + " : " + scoreDoc.score);
        }
    }

    private static Query createQuery(String field, String queryString) throws ParseException {
        QueryParser parser = new QueryParser(field,
                new WhitespaceAnalyzer());
        Query query = parser.parse(queryString);
        return query;
    }

    static IndexWriter createWriter(Directory directory) throws IOException {
        Analyzer analyzer = createIndexAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, config);
    }

    static void createDocuments(IndexWriter writer) throws IOException {

        Document dl4s = new Document();
        dl4s.add(new TextField("pages", "Aeroplane", Field.Store.YES));
        dl4s.add(new TextField("title", "I like pleasure spiked with pain and music is my aeroplane ...", Field.Store.YES));
        writer.addDocument(dl4s);

        writer.addDocument(dl4s);

        writer.commit();
        writer.close();
    }
}
