package io.github.inflationx.viewpump;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public interface FallbackViewCreator {
    @Nullable
    View onCreateView(@Nullable View parent, String name, Context context, @Nullable AttributeSet attrs);
}
