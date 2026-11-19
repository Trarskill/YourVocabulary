package stu.cn.ua.yourvocabulary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import stu.cn.ua.yourvocabulary.R;

public class SettingsFragment extends Fragment {

    private static final String KEY_SELECTED_LANG = "key_lang";
    private static final String KEY_SELECTED_COUNT = "key_count";
    private static final String KEY_SELECTED_TIMER = "key_timer";

    private NavigationRouter router;
    private RadioGroup rgLanguage;
    private RadioGroup rgQuestionsCount;
    private RadioGroup rgTimer;
    private RadioButton rbLangDe;
    private RadioButton rbCount7, rbCount10;
    private RadioButton rbTimer30, rbTimer60;

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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rgLanguage = view.findViewById(R.id.rgLanguage);
        rgQuestionsCount = view.findViewById(R.id.rgQuestionsCount);
        rgTimer = view.findViewById(R.id.rgTimer);
        rbLangDe = view.findViewById(R.id.rbLangDe);
        rbCount7 = view.findViewById(R.id.rbCount7);
        rbCount10 = view.findViewById(R.id.rbCount10);
        rbTimer30 = view.findViewById(R.id.rbTimer30);
        rbTimer60 = view.findViewById(R.id.rbTimer60);

        Button btnNext = view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> goToTestTypeScreen());

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    private void goToTestTypeScreen() {
        String language = (rbLangDe != null && rbLangDe.isChecked()) ? "DE" : "EN";

        int count = 5;
        if (rbCount7 != null && rbCount7.isChecked()) {
            count = 7;
        } else if (rbCount10 != null && rbCount10.isChecked()) {
            count = 10;
        }

        int timerSeconds = 20;
        if (rbTimer30 != null && rbTimer30.isChecked()) {
            timerSeconds = 30;
        } else if (rbTimer60 != null && rbTimer60.isChecked()) {
            timerSeconds = 60;
        }

        router.openTestTypeScreen(language, count, timerSeconds);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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