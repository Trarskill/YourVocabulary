package stu.cn.ua.yourvocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryRepository {

    // 10 слів для англійської мови
    private static final List<Word> ENGLISH_WORDS = new ArrayList<>();
    static {
        ENGLISH_WORDS.add(new Word("Apple", "Яблуко"));
        ENGLISH_WORDS.add(new Word("Book", "Книга"));
        ENGLISH_WORDS.add(new Word("House", "Будинок"));
        ENGLISH_WORDS.add(new Word("Sun", "Сонце"));
        ENGLISH_WORDS.add(new Word("Water", "Вода"));
        ENGLISH_WORDS.add(new Word("Friend", "Друг"));
        ENGLISH_WORDS.add(new Word("Time", "Час"));
        ENGLISH_WORDS.add(new Word("City", "Місто"));
        ENGLISH_WORDS.add(new Word("School", "Школа"));
        ENGLISH_WORDS.add(new Word("Freedom", "Свобода"));
    }

    // 10 слів для німецької мови
    private static final List<Word> GERMAN_WORDS = new ArrayList<>();
    static {
        GERMAN_WORDS.add(new Word("Apfel", "Яблуко"));
        GERMAN_WORDS.add(new Word("Buch", "Книга"));
        GERMAN_WORDS.add(new Word("Haus", "Будинок"));
        GERMAN_WORDS.add(new Word("Sonne", "Сонце"));
        GERMAN_WORDS.add(new Word("Wasser", "Вода"));
        GERMAN_WORDS.add(new Word("Freund", "Друг"));
        GERMAN_WORDS.add(new Word("Zeit", "Час"));
        GERMAN_WORDS.add(new Word("Stadt", "Місто"));
        GERMAN_WORDS.add(new Word("Schule", "Школа"));
        GERMAN_WORDS.add(new Word("Freiheit", "Свобода"));
    }

    /**
     * Повертає список випадкових, неповторюваних слів заданої кількості.
     */
    public static List<Word> getWords(String language, int count) {
        List<Word> source = "DE".equalsIgnoreCase(language) ? GERMAN_WORDS : ENGLISH_WORDS;
        List<Word> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled);

        int limit = Math.min(count, shuffled.size());
        return new ArrayList<>(shuffled.subList(0, limit));
    }

    /**
     * Повертає варіанти неправильних відповідей для тесту з варіантами.
     */
    public static List<String> getWrongTranslations(String language, String correctTranslation, int neededCount) {
        List<Word> source = "DE".equalsIgnoreCase(language) ? GERMAN_WORDS : ENGLISH_WORDS;
        List<String> wrongAnswers = new ArrayList<>();

        List<Word> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled);

        for (Word word : shuffled) {
            if (!word.getTranslation().equalsIgnoreCase(correctTranslation)) {
                wrongAnswers.add(word.getTranslation());
                if (wrongAnswers.size() == neededCount) {
                    break;
                }
            }
        }
        return wrongAnswers;
    }
}