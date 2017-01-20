package io.github.inflationx.viewpump.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class AnotherTestView extends View {

    public static final String NAME = AnotherTestView.class.getName();

    public AnotherTestView(Context context) {
        super(context);
    }

    public AnotherTestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}