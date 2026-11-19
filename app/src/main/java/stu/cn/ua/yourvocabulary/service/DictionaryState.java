package stu.cn.ua.yourvocabulary.service;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import stu.cn.ua.yourvocabulary.Word;

public class DictionaryState {

    private static DictionaryState instance;

    private boolean isLoading = false;
    private List<Word> loadedWords = Collections.emptyList();
    private final Set<Listener> listeners = new HashSet<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DictionaryState() {
    }

    public static synchronized DictionaryState getInstance() {
        if (instance == null) {
            instance = new DictionaryState();
        }
        return instance;
    }

    @MainThread
    public void addListener(Listener listener) {
        listeners.add(listener);
        if (isLoading) {
            listener.onLoadingStarted();
        } else if (!loadedWords.isEmpty()) {
            listener.onWordsLoaded(loadedWords);
        }
    }

    @MainThread
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void notifyLoadingStarted() {
        isLoading = true;
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onLoadingStarted();
            }
        });
    }

    public void notifyWordsLoaded(List<Word> words) {
        isLoading = false;
        this.loadedWords = new ArrayList<>(words);
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onWordsLoaded(this.loadedWords);
            }
        });
    }

    public void notifyLoadingError(String errorMessage) {
        isLoading = false;
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onLoadingError(errorMessage);
            }
        });
    }

    public void clear() {
        loadedWords = Collections.emptyList();
        isLoading = false;
    }

    public interface Listener {
        void onLoadingStarted();
        void onWordsLoaded(List<Word> words);
        void onLoadingError(String message);
    }
}