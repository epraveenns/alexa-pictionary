package com.pravinag.pictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class TestClass implements RequestHandler<Integer, String>{
    public static void main(String args[]) {
    }

    private static boolean doesInputStartsWithPreviousRandomWord(char firstChar, String previousWord)
    {
        return previousWord == null || previousWord.charAt(previousWord.length()-1) == firstChar;
    }
    private static String getInputWord() {
        Scanner sc = new Scanner(System.in);
        return sc.next();
    }

    private static Map<Character, Set<String>> getSortedDictionary() throws IOException {
        BufferedReader br = getBufferedReader();
        String currentLine;
        Map<Character, Set<String>> sortedDictionary = new HashMap<>();
        while ((currentLine = br.readLine()) != null) {
            char key = currentLine.charAt(0);
            if (sortedDictionary.containsKey(key)) {
                Set<String> set = sortedDictionary.get(key);
                set.add(currentLine);
            } else {
                Set<String> set = new HashSet<>(10000);
                set.add(currentLine);
                sortedDictionary.put(key, set);
            }
        }
        return sortedDictionary;
    }

    private static BufferedReader getBufferedReader() throws FileNotFoundException {
        File file = new File("words_alpha.txt");
        return new BufferedReader(new FileReader(file));
    }

    @Override
    public String handleRequest(Integer integer, Context context) {
        Map<Character, Set<String>> sortedDictionary = null;
        try {
            sortedDictionary = getSortedDictionary();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> alreadyUsedWords = new HashSet<>(1000);

        System.out.println("Welcome. Start the game by entering any word");
        String randomWord = null;
        while (true) {
            String inputWord = getInputWord();
            char firstChar = inputWord.charAt(0);
            Set<String> wordsStartingWithFirstChar = sortedDictionary.get(firstChar);
            if (doesInputStartsWithPreviousRandomWord(firstChar, randomWord) && !alreadyUsedWords.contains(inputWord) && wordsStartingWithFirstChar.contains(inputWord)) {
                alreadyUsedWords.add(inputWord);
                char lastChar = inputWord.charAt(inputWord.length() - 1);
                Set<String> wordsToChooseFrom = sortedDictionary.get(lastChar);
                String[] wordsToChooseFromArray = wordsToChooseFrom.toArray(new String[0]);
                int count = wordsToChooseFrom.size();
                do {
                    int randomNumber = ThreadLocalRandom.current().nextInt(0, wordsToChooseFrom.size());
                    randomWord = wordsToChooseFromArray[randomNumber];
                } while (alreadyUsedWords.contains(randomWord) && count-- > 0);

                if (count < 0) {
                    System.out.println("You Won");
                    break;
                } else
                {
                    alreadyUsedWords.add(randomWord);
                    System.out.println(randomWord);
                }
            } else {
                System.out.println("Invalid word");
                break;
            }
        }
        return "Thank you";
    }
}