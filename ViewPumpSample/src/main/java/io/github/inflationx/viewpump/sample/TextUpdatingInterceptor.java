package io.github.inflationx.viewpump.sample;

import android.content.res.TypedArray;

import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

/**
 * This is an example of a post-inflation interceptor that modifies the properties of a view
 * after it has been created. Here we prefix the text for any view that has been replaced with
 * a custom version by the {@link CustomTextViewInterceptor}.
 */
public class TextUpdatingInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateResult result = chain.proceed(chain.request());
        if (result.view() instanceof CustomTextView) {
            CustomTextView textView = (CustomTextView) result.view();

            TypedArray a = result.context().obtainStyledAttributes(result.attrs(), new int[]{android.R.attr.text});
            try {
                CharSequence text = a.getText(0);
                if (text != null && text.length() > 0) {
                    if (text.toString().startsWith("\n")) {
                        text = text.toString().substring(1);
                    }
                    textView.setText(textView.getContext().getString(R.string.custom_textview_prefixed_text, text));
                }
            } finally {
                a.recycle();
            }
        }
        return result;
    }
}
