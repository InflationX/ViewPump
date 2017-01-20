package io.github.inflationx.viewpump;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

interface ViewPumpActivityFactory {

    /**
     * Used to Wrap the Activity onCreateView method.
     *
     * You implement this method like so in you base activity.
     * <pre>
     * {@code
     * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
     *   return ViewPumpContextWrapper.get(getBaseContext()).onActivityCreateView(super.onCreateView(parent, name,context, attrs), attrs);
     * }
     * }
     * </pre>
     *
     * @param parent  parent view, can be null.
     * @param view    result of {@code super.onCreateView(parent, name, context, attrs)}, this might be null, which is fine.
     * @param name    Name of View we are trying to inflate
     * @param context current context (normally the Activity's)
     * @param attrs   see {@link android.view.LayoutInflater.Factory2#onCreateView(View, String, Context, AttributeSet)}  @return the result from the activities {@code onCreateView()}
     * @return The view passed in, or null if nothing was passed in.
     * @see android.view.LayoutInflater.Factory2
     */
    @Nullable
    View onActivityCreateView(View parent, View view, String name, Context context, AttributeSet attrs);
}
