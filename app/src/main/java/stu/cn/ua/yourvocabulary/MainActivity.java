package stu.cn.ua.yourvocabulary;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import stu.cn.ua.yourvocabulary.fragments.NavigationRouter;
import stu.cn.ua.yourvocabulary.fragments.SettingsFragment;
import stu.cn.ua.yourvocabulary.fragments.TestFragment;
import stu.cn.ua.yourvocabulary.fragments.TestTypeFragment;
import stu.cn.ua.yourvocabulary.service.DictionaryState;

public class MainActivity extends AppCompatActivity implements NavigationRouter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void openTestTypeScreen(String language, int questionCount, int timerSeconds) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        TestTypeFragment.newInstance(language, questionCount, timerSeconds))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openTestScreen(String mode, String language, int questionCount, int timerSeconds) {
        DictionaryState.getInstance().clear();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        TestFragment.newInstance(mode, language, questionCount, timerSeconds))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateBack() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    public void returnToSettings() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}