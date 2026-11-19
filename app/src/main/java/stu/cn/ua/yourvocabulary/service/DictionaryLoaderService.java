package stu.cn.ua.yourvocabulary.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stu.cn.ua.yourvocabulary.Word;

public class DictionaryLoaderService extends IntentService {

    private static final String TAG = DictionaryLoaderService.class.getSimpleName();
    private static final String ACTION_LOAD_WORDS = "stu.cn.ua.yourvocabulary.ACTION_LOAD_WORDS";
    private static final String EXTRA_LANGUAGE = "EXTRA_LANGUAGE";
    private static final String DICTIONARY_FILE = "words.txt";

    public DictionaryLoaderService() {
        super(TAG);
    }

    public static void startLoading(Context context, String language) {
        Intent intent = new Intent(context, DictionaryLoaderService.class);
        intent.setAction(ACTION_LOAD_WORDS);
        intent.putExtra(EXTRA_LANGUAGE, language);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || !ACTION_LOAD_WORDS.equals(intent.getAction())) {
            return;
        }

        String targetLanguage = intent.getStringExtra(EXTRA_LANGUAGE);
        if (targetLanguage == null) {
            targetLanguage = "EN";
        }

        Log.d(TAG, "onHandleIntent: loading words for language = " + targetLanguage);
        DictionaryState state = DictionaryState.getInstance();
        state.notifyLoadingStarted();

        try {
            // Невелика затримка для наочної демонстрації асинхронної роботи служби
            Thread.sleep(500);

            List<Word> filteredWords = new ArrayList<>();
            try (InputStream is = getAssets().open(DICTIONARY_FILE);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(";");
                    if (parts.length == 3 && parts[0].equalsIgnoreCase(targetLanguage)) {
                        filteredWords.add(new Word(parts[1].trim(), parts[2].trim()));
                    }
                }
            }

            Collections.shuffle(filteredWords);
            Log.d(TAG, "Loaded " + filteredWords.size() + " words from " + DICTIONARY_FILE);
            state.notifyWordsLoaded(filteredWords);

        } catch (Exception e) {
            Log.e(TAG, "Failed to load words from file", e);
            state.notifyLoadingError("Помилка читання словника: " + e.getMessage());
        }
    }
}