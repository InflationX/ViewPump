package io.github.inflationx.viewpump.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class TestView extends View {

    public static final String NAME = TestView.class.getName();

    private Context context;

    private boolean isPostProcessed;

    public TestView(Context context) {
        super(context);
        this.context = context;
        isPostProcessed = false;
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        isPostProcessed = false;
    }

    public boolean isSameContextAs(Context context) {
        return this.context == context;
    }

    public boolean isPostProcessed() {
        return isPostProcessed;
    }

    public void setPostProcessed(boolean postProcessed) {
        isPostProcessed = postProcessed;
    }
}