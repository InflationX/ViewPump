package io.github.inflationx.viewpump;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface FallbackViewCreator {
    @Nullable
    View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @Nullable AttributeSet attrs);
}
