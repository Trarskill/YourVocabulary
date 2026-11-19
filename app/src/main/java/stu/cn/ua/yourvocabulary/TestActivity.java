package stu.cn.ua.yourvocabulary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import stu.cn.ua.yourvocabulary.R;

public class TestActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_CHOICE = "mode_choice";
    public static final String MODE_INPUT = "mode_input";

    private String mode;
    private String language;
    private int totalQuestions;
    private int timerSecondsSetting;

    private List<Word> wordList;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int wrongCount = 0;

    private CountDownTimer timer;
    private long timeRemainingMillis;
    private boolean isAnswered = false;

    // UI Елементи
    private TextView tvTimer, tvQuestionCounter, tvOriginalWord, tvCorrectAnswer, tvUserAnswer;
    private LinearLayout layoutChoice, layoutInput;
    private Button[] optionButtons = new Button[4];
    private TextInputLayout tilAnswer;
    private TextInputEditText etAnswer;
    private Button btnCheckInput, btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_test), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int screenPadding = getResources().getDimensionPixelSize(R.dimen.padding_screen);
            v.setPadding(
                    systemBars.left + screenPadding,
                    systemBars.top + screenPadding,
                    systemBars.right + screenPadding,
                    systemBars.bottom + screenPadding);
            return insets;
        });

        readIntentExtras();
        initViews();
        setupModeUI();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            wordList = DictionaryRepository.getWords(language, totalQuestions);
            timeRemainingMillis = timerSecondsSetting * 1000L;
            loadQuestion();
        }
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            mode = intent.getStringExtra(EXTRA_MODE);
            language = intent.getStringExtra(SettingsActivity.EXTRA_LANGUAGE);
            totalQuestions = intent.getIntExtra(SettingsActivity.EXTRA_QUESTION_COUNT, 5);
            timerSecondsSetting = intent.getIntExtra(SettingsActivity.EXTRA_TIMER_SECONDS, 20);
        }
        if (mode == null) mode = MODE_CHOICE;
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvOriginalWord = findViewById(R.id.tvOriginalWord);
        tvCorrectAnswer = findViewById(R.id.tvCorrectAnswer);
        tvUserAnswer = findViewById(R.id.tvUserAnswer);

        layoutChoice = findViewById(R.id.layoutChoiceContainer);
        layoutInput = findViewById(R.id.layoutInputContainer);

        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        tilAnswer = findViewById(R.id.tilAnswer);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckInput = findViewById(R.id.btnCheckInput);
        btnContinue = findViewById(R.id.btnContinue);

        findViewById(R.id.btnExitTest).setOnClickListener(v -> finish());
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

    private void loadQuestion() {
        if (currentIndex >= wordList.size()) {
            showFinalResults();
            return;
        }

        isAnswered = false;

        // Початковий стан кнопки: сіра та неактивна
        setButtonState(false, R.color.btn_disabled_bg, R.color.btn_disabled_text);

        tvCorrectAnswer.setVisibility(View.GONE);
        tvUserAnswer.setVisibility(View.GONE);

        Word currentWord = wordList.get(currentIndex);
        tvOriginalWord.setText(currentWord.getOriginal());
        tvQuestionCounter.setText(getString(R.string.question_counter, currentIndex + 1, wordList.size()));

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

    private void setupChoiceOptions(Word currentWord) {
        List<String> options = DictionaryRepository.getWrongTranslations(language, currentWord.getTranslation(), 3);
        options.add(currentWord.getTranslation());
        Collections.shuffle(options);

        int normalBg = ContextCompat.getColor(this, R.color.color_neutral);
        int textColor = ContextCompat.getColor(this, R.color.textColorPrimary);

        for (int i = 0; i < optionButtons.length; i++) {
            Button btn = optionButtons[i];
            btn.setText(options.get(i));
            btn.setEnabled(true);
            btn.setBackgroundTintList(ColorStateList.valueOf(normalBg));
            btn.setTextColor(textColor);

            final String selectedOption = options.get(i);
            btn.setOnClickListener(v -> checkChoiceAnswer(selectedOption, currentWord.getTranslation()));
        }
    }

    private void checkChoiceAnswer(String selectedOption, String correctOption) {
        if (isAnswered) return;
        isAnswered = true;
        cancelTimer();

        int correctColor = ContextCompat.getColor(this, R.color.color_correct);
        int incorrectColor = ContextCompat.getColor(this, R.color.color_incorrect);

        for (Button btn : optionButtons) {
            btn.setEnabled(false);
            String btnText = btn.getText().toString();

            if (btnText.equalsIgnoreCase(correctOption)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(correctColor));
                btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
            if (btnText.equalsIgnoreCase(selectedOption) && !selectedOption.equalsIgnoreCase(correctOption)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(incorrectColor));
                btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
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
        String userAnswer = etAnswer.getText() != null ? etAnswer.getText().toString().trim() : "";
        if (userAnswer.isEmpty()) {
            tilAnswer.setError("Введіть відповідь!");
            return;
        }

        isAnswered = true;
        cancelTimer();

        Word currentWord = wordList.get(currentIndex);

        // Ховаємо поле введення
        layoutInput.setVisibility(View.GONE);

        // Показуємо відповідь користувача на місці поля введення
        tvUserAnswer.setVisibility(View.VISIBLE);
        tvUserAnswer.setText(getString(R.string.label_user_answer, userAnswer));
        tvCorrectAnswer.setVisibility(View.VISIBLE);
        tvCorrectAnswer.setText(getString(R.string.label_correct_answer, currentWord.getTranslation()));

        if (userAnswer.equalsIgnoreCase(currentWord.getTranslation())) {
            correctCount++;
            tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.color_correct));
            setButtonState(true, R.color.color_correct, android.R.color.white);
        } else {
            wrongCount++;
            tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.color_incorrect));
            setButtonState(true, R.color.color_incorrect, android.R.color.white);
        }
    }

    private void onTimeOut() {
        if (isAnswered) return;
        isAnswered = true;
        wrongCount++;

        Toast.makeText(this, R.string.time_out_msg, Toast.LENGTH_SHORT).show();

        Word currentWord = wordList.get(currentIndex);

        if (MODE_CHOICE.equals(mode)) {
            int correctColor = ContextCompat.getColor(this, R.color.color_correct);
            for (Button btn : optionButtons) {
                btn.setEnabled(false);
                if (btn.getText().toString().equalsIgnoreCase(currentWord.getTranslation())) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(correctColor));
                    btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                }
            }
        } else {
            layoutInput.setVisibility(View.GONE);
            tvUserAnswer.setVisibility(View.VISIBLE);
            tvUserAnswer.setText("Час вичерпано!");
            tvUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.color_incorrect));

            tvCorrectAnswer.setVisibility(View.VISIBLE);
            tvCorrectAnswer.setText(getString(R.string.label_correct_answer, currentWord.getTranslation()));
        }

        setButtonState(true, R.color.color_incorrect, android.R.color.white);
    }

    private void setButtonState(boolean enabled, int bgColorRes, int textColorRes) {
        btnContinue.setEnabled(enabled);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, bgColorRes)));
        btnContinue.setTextColor(ContextCompat.getColor(this, textColorRes));
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
                timeRemainingMillis = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), getString(R.string.timer_format), seconds));
            }

            @Override
            public void onFinish() {
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_result_title)
                .setMessage(getString(R.string.dialog_result_message, correctCount, wordList.size(), wrongCount))
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_result_ok, (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cancelTimer();
        outState.putParcelableArrayList("wordList", new ArrayList<>(wordList));
        outState.putInt("currentIndex", currentIndex);
        outState.putInt("correctCount", correctCount);
        outState.putInt("wrongCount", wrongCount);
        outState.putLong("timeRemaining", timeRemainingMillis);
        outState.putBoolean("isAnswered", isAnswered);
    }

    private void restoreState(Bundle savedInstanceState) {
        wordList = savedInstanceState.getParcelableArrayList("wordList");
        currentIndex = savedInstanceState.getInt("currentIndex", 0);
        correctCount = savedInstanceState.getInt("correctCount", 0);
        wrongCount = savedInstanceState.getInt("wrongCount", 0);
        timeRemainingMillis = savedInstanceState.getLong("timeRemaining", timerSecondsSetting * 1000L);
        isAnswered = savedInstanceState.getBoolean("isAnswered", false);

        loadQuestion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}