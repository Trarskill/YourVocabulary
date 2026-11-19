package stu.cn.ua.yourvocabulary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import stu.cn.ua.yourvocabulary.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String EXTRA_LANGUAGE = "extra_language";
    public static final String EXTRA_QUESTION_COUNT = "extra_question_count";
    public static final String EXTRA_TIMER_SECONDS = "extra_timer_seconds";

    private static final String KEY_SELECTED_LANG = "key_lang";
    private static final String KEY_SELECTED_COUNT = "key_count";
    private static final String KEY_SELECTED_TIMER = "key_timer";

    private RadioGroup rgLanguage;
    private RadioGroup rgQuestionsCount;
    private RadioGroup rgTimer;

    private RadioButton rbLangDe;
    private RadioButton rbCount7, rbCount10;
    private RadioButton rbTimer30, rbTimer60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Налаштування відступів Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    private void initViews() {
        rgLanguage = findViewById(R.id.rgLanguage);
        rgQuestionsCount = findViewById(R.id.rgQuestionsCount);
        rgTimer = findViewById(R.id.rgTimer);

        rbLangDe = findViewById(R.id.rbLangDe);

        rbCount7 = findViewById(R.id.rbCount7);
        rbCount10 = findViewById(R.id.rbCount10);

        rbTimer30 = findViewById(R.id.rbTimer30);
        rbTimer60 = findViewById(R.id.rbTimer60);
    }

    private void setupClickListeners() {
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> goToTestTypeScreen());
    }

    private void goToTestTypeScreen() {
        Intent intent = new Intent(this, TestTypeActivity.class);

        // Мова
        String language = (rbLangDe != null && rbLangDe.isChecked()) ? "DE" : "EN";

        // Кількість питань
        int count = 5;
        if (rbCount7 != null && rbCount7.isChecked()) {
            count = 7;
        } else if (rbCount10 != null && rbCount10.isChecked()) {
            count = 10;
        }

        // Час таймера
        int timerSeconds = 20;
        if (rbTimer30 != null && rbTimer30.isChecked()) {
            timerSeconds = 30;
        } else if (rbTimer60 != null && rbTimer60.isChecked()) {
            timerSeconds = 60;
        }

        intent.putExtra(EXTRA_LANGUAGE, language);
        intent.putExtra(EXTRA_QUESTION_COUNT, count);
        intent.putExtra(EXTRA_TIMER_SECONDS, timerSeconds);

        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (rgLanguage != null) outState.putInt(KEY_SELECTED_LANG, rgLanguage.getCheckedRadioButtonId());
        if (rgQuestionsCount != null) outState.putInt(KEY_SELECTED_COUNT, rgQuestionsCount.getCheckedRadioButtonId());
        if (rgTimer != null) outState.putInt(KEY_SELECTED_TIMER, rgTimer.getCheckedRadioButtonId());
    }

    private void restoreState(Bundle savedInstanceState) {
        int langId = savedInstanceState.getInt(KEY_SELECTED_LANG, -1);
        if (langId != -1 && rgLanguage != null) rgLanguage.check(langId);

        int countId = savedInstanceState.getInt(KEY_SELECTED_COUNT, -1);
        if (countId != -1 && rgQuestionsCount != null) rgQuestionsCount.check(countId);

        int timerId = savedInstanceState.getInt(KEY_SELECTED_TIMER, -1);
        if (timerId != -1 && rgTimer != null) rgTimer.check(timerId);
    }
}