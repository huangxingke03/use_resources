package com.example.resources.video;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import android.util.AttributeSet;

public class ExoPositionView extends AppCompatTextView {
    public ExoPositionView(Context context) {
        this(context, null, 0);
    }

    public ExoPositionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPositionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (mOnTextChangeListener != null) {
            mOnTextChangeListener.onTextChanged(text);
        }
    }
    private OnTextChangeListener mOnTextChangeListener;
    public void setOnTextChangeListener(OnTextChangeListener inf) {
        mOnTextChangeListener = inf;
    }
    public interface OnTextChangeListener {
        void onTextChanged(CharSequence text);
    }
}
