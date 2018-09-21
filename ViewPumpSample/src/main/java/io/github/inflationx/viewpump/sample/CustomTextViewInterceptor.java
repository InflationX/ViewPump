package io.github.inflationx.viewpump.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import io.github.inflationx.viewpump.InflateRequest;
import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

/**
 * This is an example of a pre-inflation interceptor that returns programmatically instantiated
 * CustomTextViews instead of inflating TextViews.
 */
public class CustomTextViewInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateRequest request = chain.request();
        View view = inflateView(request.name(), request.context(), request.attrs());

        if (view != null) {
            return InflateResult.builder()
                    .view(view)
                    .name(view.getClass().getName())
                    .context(request.context())
                    .attrs(request.attrs())
                    .build();
        } else {
            return chain.proceed(request);
        }
    }

    @Nullable
    private View inflateView(String name, Context context, AttributeSet attrs) {
        if ("TextView".equals(name)) {
            return new CustomTextView(context, attrs);
        }
        return null;
    }
}
