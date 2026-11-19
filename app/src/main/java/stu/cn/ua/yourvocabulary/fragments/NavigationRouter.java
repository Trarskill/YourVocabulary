package stu.cn.ua.yourvocabulary.fragments;

public interface NavigationRouter {
    void openTestTypeScreen(String language, int questionCount, int timerSeconds);
    void openTestScreen(String mode, String language, int questionCount, int timerSeconds);
    void navigateBack();
    void returnToSettings();
}