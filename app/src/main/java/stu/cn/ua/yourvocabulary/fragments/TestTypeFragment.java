package stu.cn.ua.yourvocabulary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import stu.cn.ua.yourvocabulary.R;

public class TestTypeFragment extends Fragment {

    private static final String ARG_LANGUAGE = "arg_language";
    private static final String ARG_QUESTION_COUNT = "arg_question_count";
    private static final String ARG_TIMER_SECONDS = "arg_timer_seconds";

    private NavigationRouter router;
    private String language;
    private int questionCount;
    private int timerSeconds;

    public static TestTypeFragment newInstance(String language, int questionCount, int timerSeconds) {
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE, language);
        args.putInt(ARG_QUESTION_COUNT, questionCount);
        args.putInt(ARG_TIMER_SECONDS, timerSeconds);
        TestTypeFragment fragment = new TestTypeFragment();
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
        return inflater.inflate(R.layout.fragment_test_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            language = getArguments().getString(ARG_LANGUAGE, "EN");
            questionCount = getArguments().getInt(ARG_QUESTION_COUNT, 5);
            timerSeconds = getArguments().getInt(ARG_TIMER_SECONDS, 20);
        }

        Button btnBack = view.findViewById(R.id.btnBackToSettings);
        btnBack.setOnClickListener(v -> router.navigateBack());

        MaterialCardView cardChoice = view.findViewById(R.id.cardChoiceTest);
        Button btnChoice = view.findViewById(R.id.btnStartChoiceTest);
        View.OnClickListener startChoice = v ->
                router.openTestScreen(TestFragment.MODE_CHOICE, language, questionCount, timerSeconds);
        cardChoice.setOnClickListener(startChoice);
        btnChoice.setOnClickListener(startChoice);

        MaterialCardView cardInput = view.findViewById(R.id.cardInputTest);
        Button btnInput = view.findViewById(R.id.btnStartInputTest);
        View.OnClickListener startInput = v ->
                router.openTestScreen(TestFragment.MODE_INPUT, language, questionCount, timerSeconds);
        cardInput.setOnClickListener(startInput);
        btnInput.setOnClickListener(startInput);
    }
}