package org.example;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;

public class Word2VecExample {
    public static void main(String[] args) throws URISyntaxException, FileNotFoundException {

        Word2Vec vec = getWord2Vec("small_sheckcspere.txt", 5, 100, 1);
        tryWords(vec);
        vec = null; // Release the reference

        Word2Vec vec2 = getWord2Vec("medium_data.txt", 5, 100, 1);
        tryWords(vec2);
        vec2 = null; // Release the reference

        // will be added after git file optimization
        //Word2Vec vec3 = getWord2Vec("large-data-imdb-review.csv", 5, 100, 1);
        //tryWords(vec3);
        //vec3 = null; // Release the reference

        // will be added after git file optimization
        //Word2Vec vec4 = getWord2Vec("x-large-amazon-reviews.csv", 5, 100, 10);
        //tryWords(vec4);
        //vec4 = null; // Release the reference

        System.gc(); // Suggest the JVM to run garbage collection
    }

    private static void tryWords(Word2Vec vec) {
        String[] words = new String[]{"good", "bad", "pizza", "train", "piano", "he", "harness", "go"};
        for (String w : words) {
            Collection<String> lst = vec.wordsNearest(w, 5);
            System.out.println("5 Words closest to '" + w + "': " + lst);
        }
    }

    private static Word2Vec getWord2Vec(String name, int windowSize, int layerSize, int minWordFrequency) throws URISyntaxException, FileNotFoundException {
        URL resourceUrl = Word2VecExample.class.getClassLoader().getResource(name);
        if (resourceUrl == null) {
            throw new RuntimeException("Cannot find the specified resource.");
        }
        String filePath = Paths.get(resourceUrl.toURI()).toString();
        SentenceIterator iter = new BasicLineIterator(filePath);
        Word2Vec vec = new Word2Vec.Builder()
                .layerSize(layerSize)
                .windowSize(windowSize)
                .iterate(iter)
                .minWordFrequency(minWordFrequency) // Reduce vocabulary size by setting a minimum word frequency
                .elementsLearningAlgorithm(new CBOW<>())
                .build();
        long l = System.currentTimeMillis();
        vec.fit();
        System.out.println("Training time : " + (System.currentTimeMillis() - l));
        return vec;
    }
}
