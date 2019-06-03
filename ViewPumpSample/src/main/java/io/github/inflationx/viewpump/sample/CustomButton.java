package io.github.inflationx.viewpump.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

@SuppressLint("AppCompatCustomView")
public class CustomButton extends AppCompatButton {

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
