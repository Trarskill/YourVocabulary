package stu.cn.ua.yourvocabulary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import stu.cn.ua.yourvocabulary.R;

public class TestTypeActivity extends AppCompatActivity {

    private String language;
    private int questionCount;
    private int timerSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_type);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_test_type), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Отримуємо налаштування з Intent
        Intent intent = getIntent();
        if (intent != null) {
            language = intent.getStringExtra(SettingsActivity.EXTRA_LANGUAGE);
            questionCount = intent.getIntExtra(SettingsActivity.EXTRA_QUESTION_COUNT, 5);
            timerSeconds = intent.getIntExtra(SettingsActivity.EXTRA_TIMER_SECONDS, 20);
        }

        // Кнопка повернення до налаштувань
        Button btnBack = findViewById(R.id.btnBackToSettings);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Елементи режиму з варіантами (MODE_CHOICE)
        MaterialCardView cardChoice = findViewById(R.id.cardChoiceTest);
        Button btnChoice = findViewById(R.id.btnStartChoiceTest);

        if (cardChoice != null) {
            cardChoice.setOnClickListener(v -> startTestMode(TestActivity.MODE_CHOICE));
        }
        if (btnChoice != null) {
            btnChoice.setOnClickListener(v -> startTestMode(TestActivity.MODE_CHOICE));
        }

        // 2. Елементи режиму з введенням (MODE_INPUT)
        MaterialCardView cardInput = findViewById(R.id.cardInputTest);
        Button btnInput = findViewById(R.id.btnStartInputTest);

        if (cardInput != null) {
            cardInput.setOnClickListener(v -> startTestMode(TestActivity.MODE_INPUT));
        }
        if (btnInput != null) {
            btnInput.setOnClickListener(v -> startTestMode(TestActivity.MODE_INPUT));
        }
    }

    // Єдиний метод для запуску TestActivity з передачею обраного режиму
    private void startTestMode(String mode) {
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(TestActivity.EXTRA_MODE, mode);
        intent.putExtra(SettingsActivity.EXTRA_LANGUAGE, language);
        intent.putExtra(SettingsActivity.EXTRA_QUESTION_COUNT, questionCount);
        intent.putExtra(SettingsActivity.EXTRA_TIMER_SECONDS, timerSeconds);
        startActivity(intent);
    }
}