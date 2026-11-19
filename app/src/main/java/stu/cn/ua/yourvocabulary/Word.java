package stu.cn.ua.yourvocabulary;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    private final String original;
    private final String translation;

    public Word(String original, String translation) {
        this.original = original;
        this.translation = translation;
    }

    protected Word(Parcel in) {
        original = in.readString();
        translation = in.readString();
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public String getOriginal() {
        return original;
    }

    public String getTranslation() {
        return translation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(original);
        dest.writeString(translation);
    }
}