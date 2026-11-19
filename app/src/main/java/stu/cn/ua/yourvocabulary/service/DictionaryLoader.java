package stu.cn.ua.yourvocabulary.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import stu.cn.ua.yourvocabulary.Word;

public class DictionaryLoader {

    private static final String TAG = DictionaryLoader.class.getSimpleName();
    private static final String DICTIONARY_FILE = "words.txt";

    private static DictionaryLoader instance;

    private final ExecutorService executorService;
    private final Handler mainHandler;
    private Future<?> currentFuture;

    private DictionaryLoader() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DictionaryLoader getInstance() {
        if (instance == null) {
            instance = new DictionaryLoader();
        }
        return instance;
    }

    public void loadWords(Context context, String language, LoadCallback callback) {
        if (currentFuture != null && !currentFuture.isDone()) {
            currentFuture.cancel(true);
        }

        Context appContext = context.getApplicationContext();
        String targetLanguage = (language != null) ? language : "EN";

        mainHandler.post(callback::onLoadingStarted);

        Callable<List<Word>> loadTask = () -> {
            Log.d(TAG, "Loading words for " + targetLanguage
                    + " in thread ID: " + Thread.currentThread().getId());

            // Невелика затримка для демонстрації асинхронного виконання в ExecutorService
            Thread.sleep(500);

            List<Word> filteredWords = new ArrayList<>();
            try (InputStream is = appContext.getAssets().open(DICTIONARY_FILE);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Task cancelled");
                    }
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
            return filteredWords;
        };

        currentFuture = executorService.submit(() -> {
            try {
                List<Word> words = loadTask.call();
                mainHandler.post(() -> callback.onWordsLoaded(words));
            } catch (InterruptedException e) {
                Log.w(TAG, "Word loading task was interrupted");
            } catch (Exception e) {
                Log.e(TAG, "Failed to load words from file", e);
                mainHandler.post(() -> callback.onLoadingError(
                        "Помилка читання словника: " + e.getMessage()));
            }
        });
    }

    public void cancelCurrentTask() {
        if (currentFuture != null && !currentFuture.isDone()) {
            currentFuture.cancel(true);
        }
    }

    public interface LoadCallback {
        void onLoadingStarted();
        void onWordsLoaded(List<Word> words);
        void onLoadingError(String message);
    }
}