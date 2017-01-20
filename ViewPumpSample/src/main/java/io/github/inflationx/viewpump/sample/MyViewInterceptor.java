package io.github.inflationx.viewpump.sample;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.github.inflationx.viewpump.InflateRequest;
import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class MyViewInterceptor implements Interceptor {

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
            return new MyTextView(context, attrs);
        }
        return null;
    }
}
