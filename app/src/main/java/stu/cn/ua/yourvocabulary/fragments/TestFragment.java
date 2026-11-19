package stu.cn.ua.yourvocabulary.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import stu.cn.ua.yourvocabulary.R;
import stu.cn.ua.yourvocabulary.Word;
import stu.cn.ua.yourvocabulary.service.DictionaryLoader;

public class TestFragment extends Fragment implements DictionaryLoader.LoadCallback {

    public static final String MODE_CHOICE = "mode_choice";
    public static final String MODE_INPUT = "mode_input";

    private static final String ARG_MODE = "arg_mode";
    private static final String ARG_LANGUAGE = "arg_language";
    private static final String ARG_QUESTION_COUNT = "arg_question_count";
    private static final String ARG_TIMER_SECONDS = "arg_timer_seconds";

    private NavigationRouter router;
    private String mode;
    private String language;
    private int totalQuestions;
    private int timerSecondsSetting;

    private List<Word> allLoadedWords = new ArrayList<>();
    private List<Word> wordList = new ArrayList<>();
    private int currentIndex = 0;
    private int correctCount = 0;
    private int wrongCount = 0;

    private CountDownTimer timer;
    private long timeRemainingMillis;
    private boolean isAnswered = false;

    private ProgressBar progressBar;
    private ScrollView scrollContent;
    private TextView tvTimer, tvQuestionCounter, tvOriginalWord, tvCorrectAnswer, tvUserAnswer;
    private RelativeLayout layoutChoice, layoutInput;
    private final Button[] optionButtons = new Button[4];
    private TextInputLayout tilAnswer;
    private TextInputEditText etAnswer;
    private Button btnCheckInput, btnContinue;

    public static TestFragment newInstance(String mode, String language,
                                           int questionCount, int timerSeconds) {
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        args.putString(ARG_LANGUAGE, language);
        args.putInt(ARG_QUESTION_COUNT, questionCount);
        args.putInt(ARG_TIMER_SECONDS, timerSeconds);
        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        router = (NavigationRouter) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readArgs();
        initViews(view);
        setupModeUI();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            // Асинхронне завантаження слів з файлу за допомогою ExecutorService
            DictionaryLoader.getInstance().loadWords(requireContext(), language, this);
        }
    }

    private void readArgs() {
        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, MODE_CHOICE);
            language = getArguments().getString(ARG_LANGUAGE, "EN");
            totalQuestions = getArguments().getInt(ARG_QUESTION_COUNT, 5);
            timerSecondsSetting = getArguments().getInt(ARG_TIMER_SECONDS, 20);
        }
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        scrollContent = view.findViewById(R.id.scrollContent);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvQuestionCounter = view.findViewById(R.id.tvQuestionCounter);
        tvOriginalWord = view.findViewById(R.id.tvOriginalWord);
        tvCorrectAnswer = view.findViewById(R.id.tvCorrectAnswer);
        tvUserAnswer = view.findViewById(R.id.tvUserAnswer);
        layoutChoice = view.findViewById(R.id.layoutChoiceContainer);
        layoutInput = view.findViewById(R.id.layoutInputContainer);

        optionButtons[0] = view.findViewById(R.id.btnOption1);
        optionButtons[1] = view.findViewById(R.id.btnOption2);
        optionButtons[2] = view.findViewById(R.id.btnOption3);
        optionButtons[3] = view.findViewById(R.id.btnOption4);

        tilAnswer = view.findViewById(R.id.tilAnswer);
        etAnswer = view.findViewById(R.id.etAnswer);
        btnCheckInput = view.findViewById(R.id.btnCheckInput);
        btnContinue = view.findViewById(R.id.btnContinue);

        view.findViewById(R.id.btnExitTest).setOnClickListener(v -> router.navigateBack());
        btnContinue.setOnClickListener(v -> goToNextQuestion());
        btnCheckInput.setOnClickListener(v -> checkInputAnswer());
    }

    private void setupModeUI() {
        if (MODE_INPUT.equals(mode)) {
            layoutChoice.setVisibility(View.GONE);
            layoutInput.setVisibility(View.VISIBLE);
        } else {
            layoutChoice.setVisibility(View.VISIBLE);
            layoutInput.setVisibility(View.GONE);
        }
        tvCorrectAnswer.setVisibility(View.GONE);
        tvUserAnswer.setVisibility(View.GONE);
    }

    // --- Реалізація DictionaryLoader.LoadCallback ---

    @Override
    public void onLoadingStarted() {
        if (!isAdded()) return;
        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
    }

    @Override
    public void onWordsLoaded(List<Word> words) {
        if (!isAdded()) return;
        progressBar.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);

        if (!wordList.isEmpty()) {
            return;
        }

        allLoadedWords = new ArrayList<>(words);
        int limit = Math.min(totalQuestions, allLoadedWords.size());
        wordList = new ArrayList<>(allLoadedWords.subList(0, limit));
        timeRemainingMillis = timerSecondsSetting * 1000L;
        loadQuestion();
    }

    @Override
    public void onLoadingError(String message) {
        if (!isAdded()) return;
        progressBar.setVisibility(View.GONE);
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        router.navigateBack();
    }

    // --- Логіка тестування ---

    private void loadQuestion() {
        if (wordList == null || wordList.isEmpty()) return;
        if (currentIndex >= wordList.size()) {
            showFinalResults();
            return;
        }

        isAnswered = false;
        setButtonState(false, R.color.btn_disabled_bg, R.color.btn_disabled_text);
        tvCorrectAnswer.setVisibility(View.GONE);
        tvUserAnswer.setVisibility(View.GONE);

        Word currentWord = wordList.get(currentIndex);
        tvOriginalWord.setText(currentWord.getOriginal());
        tvQuestionCounter.setText(getString(R.string.question_counter,
                currentIndex + 1, wordList.size()));

        if (MODE_CHOICE.equals(mode)) {
            layoutChoice.setVisibility(View.VISIBLE);
            setupChoiceOptions(currentWord);
        } else {
            layoutInput.setVisibility(View.VISIBLE);
            etAnswer.setText("");
            etAnswer.setEnabled(true);
            btnCheckInput.setEnabled(true);
            tilAnswer.setError(null);
        }

        startTimer(timerSecondsSetting * 1000L);
    }

    private List<String> getWrongTranslations(String correctTranslation, int neededCount) {
        List<String> wrongAnswers = new ArrayList<>();
        List<Word> shuffled = new ArrayList<>(allLoadedWords);
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

    private void setupChoiceOptions(Word currentWord) {
        List<String> options = getWrongTranslations(currentWord.getTranslation(), 3);
        options.add(currentWord.getTranslation());
        Collections.shuffle(options);

        int normalBg = ContextCompat.getColor(requireContext(), R.color.color_neutral);
        int textColor = ContextCompat.getColor(requireContext(), R.color.textColorPrimary);

        for (int i = 0; i < optionButtons.length; i++) {
            Button btn = optionButtons[i];
            btn.setText(options.get(i));
            btn.setEnabled(true);
            btn.setBackgroundTintList(ColorStateList.valueOf(normalBg));
            btn.setTextColor(textColor);

            final String selectedOption = options.get(i);
            btn.setOnClickListener(v ->
                    checkChoiceAnswer(selectedOption, currentWord.getTranslation()));
        }
    }

    private void checkChoiceAnswer(String selectedOption, String correctOption) {
        if (isAnswered) return;
        isAnswered = true;
        cancelTimer();

        int correctColor = ContextCompat.getColor(requireContext(), R.color.color_correct);
        int incorrectColor = ContextCompat.getColor(requireContext(), R.color.color_incorrect);

        for (Button btn : optionButtons) {
            btn.setEnabled(false);
            String btnText = btn.getText().toString();
            if (btnText.equalsIgnoreCase(correctOption)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(correctColor));
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            }
            if (btnText.equalsIgnoreCase(selectedOption) &&
                    !selectedOption.equalsIgnoreCase(correctOption)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(incorrectColor));
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            }
        }

        if (selectedOption.equalsIgnoreCase(correctOption)) {
            correctCount++;
            tvUserAnswer.setText(R.string.feedback_correct);
            tvUserAnswer.setTextColor(correctColor);
            tvUserAnswer.setVisibility(View.VISIBLE);
            setButtonState(true, R.color.color_correct, android.R.color.white);
        } else {
            wrongCount++;
            tvUserAnswer.setText(R.string.feedback_incorrect);
            tvUserAnswer.setTextColor(incorrectColor);
            tvUserAnswer.setVisibility(View.VISIBLE);
            setButtonState(true, R.color.color_incorrect, android.R.color.white);
        }
    }

    private void checkInputAnswer() {
        if (isAnswered) return;

        String userAnswer = etAnswer.getText() != null ?
                etAnswer.getText().toString().trim() : "";

        if (userAnswer.isEmpty()) {
            tilAnswer.setError("Введіть відповідь!");
            return;
        }

        isAnswered = true;
        cancelTimer();

        Word currentWord = wordList.get(currentIndex);
        layoutInput.setVisibility(View.GONE);

        tvUserAnswer.setVisibility(View.VISIBLE);
        tvUserAnswer.setText(getString(R.string.label_user_answer, userAnswer));

        tvCorrectAnswer.setVisibility(View.VISIBLE);
        tvCorrectAnswer.setText(getString(R.string.label_correct_answer,
                currentWord.getTranslation()));

        if (userAnswer.equalsIgnoreCase(currentWord.getTranslation())) {
            correctCount++;
            tvUserAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_correct));
            setButtonState(true, R.color.color_correct, android.R.color.white);
        } else {
            wrongCount++;
            tvUserAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_incorrect));
            setButtonState(true, R.color.color_incorrect, android.R.color.white);
        }
    }

    private void onTimeOut() {
        if (isAnswered || !isAdded()) return;
        isAnswered = true;
        wrongCount++;

        Toast.makeText(requireContext(), R.string.time_out_msg, Toast.LENGTH_SHORT).show();
        Word currentWord = wordList.get(currentIndex);

        if (MODE_CHOICE.equals(mode)) {
            int correctColor = ContextCompat.getColor(requireContext(), R.color.color_correct);
            for (Button btn : optionButtons) {
                btn.setEnabled(false);
                if (btn.getText().toString().equalsIgnoreCase(currentWord.getTranslation())) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(correctColor));
                    btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                }
            }
        } else {
            layoutInput.setVisibility(View.GONE);
            tvUserAnswer.setVisibility(View.VISIBLE);
            tvUserAnswer.setText(R.string.time_out_msg);
            tvUserAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_incorrect));

            tvCorrectAnswer.setVisibility(View.VISIBLE);
            tvCorrectAnswer.setText(getString(R.string.label_correct_answer,
                    currentWord.getTranslation()));
        }

        setButtonState(true, R.color.color_incorrect, android.R.color.white);
    }

    private void setButtonState(boolean enabled, int bgColorRes, int textColorRes) {
        btnContinue.setEnabled(enabled);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), bgColorRes)));
        btnContinue.setTextColor(ContextCompat.getColor(requireContext(), textColorRes));
    }

    private void goToNextQuestion() {
        currentIndex++;
        loadQuestion();
    }

    private void startTimer(long durationMillis) {
        cancelTimer();
        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return;
                timeRemainingMillis = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(),
                        getString(R.string.timer_format), seconds));
            }

            @Override
            public void onFinish() {
                if (!isAdded()) return;
                tvTimer.setText("00:00");
                onTimeOut();
            }
        }.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void showFinalResults() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_result_title)
                .setMessage(getString(R.string.dialog_result_message,
                        correctCount, wordList.size(), wrongCount))
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_result_ok,
                        (dialog, which) -> router.returnToSettings())
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        cancelTimer();
        outState.putParcelableArrayList("allLoadedWords", new ArrayList<>(allLoadedWords));
        outState.putParcelableArrayList("wordList", new ArrayList<>(wordList));
        outState.putInt("currentIndex", currentIndex);
        outState.putInt("correctCount", correctCount);
        outState.putInt("wrongCount", wrongCount);
        outState.putLong("timeRemaining", timeRemainingMillis);
        outState.putBoolean("isAnswered", isAnswered);
    }

    private void restoreState(Bundle savedInstanceState) {
        ArrayList<Word> savedAll = savedInstanceState.getParcelableArrayList("allLoadedWords");
        if (savedAll != null) allLoadedWords = savedAll;

        ArrayList<Word> savedList = savedInstanceState.getParcelableArrayList("wordList");
        if (savedList != null) wordList = savedList;

        currentIndex = savedInstanceState.getInt("currentIndex", 0);
        correctCount = savedInstanceState.getInt("correctCount", 0);
        wrongCount = savedInstanceState.getInt("wrongCount", 0);
        timeRemainingMillis = savedInstanceState.getLong("timeRemaining", timerSecondsSetting * 1000L);
        isAnswered = savedInstanceState.getBoolean("isAnswered", false);

        if (!wordList.isEmpty()) {
            loadQuestion();
        } else {
            DictionaryLoader.getInstance().loadWords(requireContext(), language, this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTimer();
        DictionaryLoader.getInstance().cancelCurrentTask();
    }
}