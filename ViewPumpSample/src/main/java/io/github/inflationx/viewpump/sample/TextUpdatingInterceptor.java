package io.github.inflationx.viewpump.sample;

import android.content.res.TypedArray;

import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class TextUpdatingInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateResult result = chain.proceed(chain.request());
        if (result.view() instanceof MyTextView) {
            MyTextView textView = (MyTextView) result.view();

            TypedArray a = result.context().obtainStyledAttributes(result.attrs(), new int[]{android.R.attr.text});
            try {
                CharSequence text = a.getText(0);
                if (text != null && text.length() > 0) {
                    if (text.toString().startsWith("\n")) {
                        text = text.toString().substring(1);
                    }
                    textView.setText("\n[MyView] " + text);
                }
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }
        return result;
    }
}
