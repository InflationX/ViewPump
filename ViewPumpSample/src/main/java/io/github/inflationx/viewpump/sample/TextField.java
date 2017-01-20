package io.github.inflationx.viewpump.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * For ViewPump.
 */
public class TextField extends TextView {

    public TextField(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.textFieldStyle);
    }

}
