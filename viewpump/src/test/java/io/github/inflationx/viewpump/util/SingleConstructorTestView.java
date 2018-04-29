package io.github.inflationx.viewpump.util;

import android.content.Context;
import android.view.View;

public class SingleConstructorTestView extends View {

    public static final String NAME = SingleConstructorTestView.class.getName();

    private Context context;

    public SingleConstructorTestView(Context context) {
        super(context);
        this.context = context;
    }

    public boolean isSameContextAs(Context context) {
        return this.context == context;
    }
}