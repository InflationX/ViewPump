package io.github.inflationx.viewpump.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.github.inflationx.viewpump.FallbackViewCreator;

public class TestFallbackViewCreator implements FallbackViewCreator {

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @Nullable AttributeSet attrs) {
        if (TestView.NAME.equals(name)) {
            return new TestView(context, attrs);
        } else if (AnotherTestView.NAME.equals(name)) {
            return new AnotherTestView(context, attrs);
        }
        return null;
    }
}