package br.gov.md.parla_md_backend.util;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class NLPUtil {

    private static final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private static POSTaggerME posTagger;
    private static DictionaryLemmatizer lemmatizer;

    static {
        try (InputStream modelIn = NLPUtil.class.getResourceAsStream("/models/pt-pos-maxent.bin");
             InputStream dictIn = NLPUtil.class.getResourceAsStream("/models/pt-lemmatizer.dict")) {
            POSModel model = new POSModel(modelIn);
            posTagger = new POSTaggerME(model);
            lemmatizer = new DictionaryLemmatizer(dictIn);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar modelos NLP", e);
        }
    }

    public static String extractMainTheme(String text) {
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);
        String[] lemmas = lemmatizer.lemmatize(tokens, tags);

        // Filtrar apenas substantivos e verbos
        List<String> keyWords = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("N") || tags[i].startsWith("V")) {
                keyWords.add(lemmas[i].toLowerCase());
            }
        }

        // Contar frequência das palavras
        Map<String, Long> wordFrequency = keyWords.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        // Encontrar a palavra mais frequente
        return wordFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Tema não identificado");
    }

    public static String analyzeSimilarity(String text1, String text2) {
        Map<String, Integer> vector1 = createVector(text1);
        Map<String, Integer> vector2 = createVector(text2);

        double similarity = calculateCosineSimilarity(vector1, vector2);

        return String.format("A similaridade entre os textos é de %.2f%%", similarity * 100);
    }

    private static Map<String, Integer> createVector(String text) {
        String[] tokens = tokenizer.tokenize(text.toLowerCase());
        String[] tags = posTagger.tag(tokens);
        String[] lemmas = lemmatizer.lemmatize(tokens, tags);

        return Arrays.stream(lemmas)
                .filter(lemma -> !lemma.equals("O")) // Filtra palavras não lematizadas
                .collect(Collectors.groupingBy(
                        w -> w,
                        Collectors.summingInt(w -> 1)
                ));
    }

    private static double calculateCosineSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            int count1 = vector1.getOrDefault(word, 0);
            int count2 = vector2.getOrDefault(word, 0);
            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }



}
