package io.github.inflationx.viewpump.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class AnotherTestView extends View {

    public static final String NAME = AnotherTestView.class.getName();

    private Context context;

    public AnotherTestView(Context context) {
        super(context);
        this.context = context;
    }

    public AnotherTestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public boolean isSameContextAs(Context context) {
        return this.context == context;
    }
}