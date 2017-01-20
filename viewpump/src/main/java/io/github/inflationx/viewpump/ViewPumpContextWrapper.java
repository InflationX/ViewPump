package io.github.inflationx.viewpump;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public final class ViewPumpContextWrapper extends ContextWrapper {

    private ViewPumpLayoutInflater mInflater;

    /**
     * Uses the default configuration from {@link ViewPump}
     *
     * Remember if you are defining default in the {@link ViewPump} make sure this
     * is initialised before the activity is created.
     *
     * @param base ContextBase to Wrap.
     * @return ContextWrapper to pass back to the activity.
     */
    public static ContextWrapper wrap(@NonNull Context base) {
        return new ViewPumpContextWrapper(base);
    }

    /**
     * You only need to call this <b>IF</b> you disable
     * {@link ViewPump.Builder#setPrivateFactoryInjectionEnabled(boolean)}
     * This will need to be called from the
     * {@link Activity#onCreateView(View, String, Context, AttributeSet)}
     * method to enable view font injection if the view is created inside the activity onCreateView.
     *
     * You would implement this method like so in you base activity.
     * <pre>
     * {@code
     * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
     *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs);
     * }
     * }
     * </pre>
     *
     * @param activity The activity the original that the ContextWrapper was attached too.
     * @param parent   Parent view from onCreateView
     * @param view     The View Created inside onCreateView or from super.onCreateView
     * @param name     The View name from onCreateView
     * @param context  The context from onCreateView
     * @param attr     The AttributeSet from onCreateView
     * @return The same view passed in, or null if null passed in.
     */
    @Nullable
    public static View onActivityCreateView(Activity activity, View parent, View view, String name, Context context, AttributeSet attr) {
        return get(activity).onActivityCreateView(parent, view, name, context, attr);
    }

    /**
     * Get the ViewPump Activity Fragment Instance to allow callbacks for when views are created.
     *
     * @param activity The activity the original that the ContextWrapper was attached too.
     * @return Interface allowing you to call onActivityViewCreated
     */
    static ViewPumpActivityFactory get(@NonNull Activity activity) {
        if (!(activity.getLayoutInflater() instanceof ViewPumpLayoutInflater)) {
            throw new RuntimeException("This activity does not wrap the Base Context! See ViewPumpContextWrapper.wrap(Context)");
        }
        return (ViewPumpActivityFactory) activity.getLayoutInflater();
    }

    /**
     * Uses the default configuration from {@link ViewPump}
     *
     * Remember if you are defining default in the
     * {@link ViewPump} make sure this is initialised before
     * the activity is created.
     *
     * @param base ContextBase to Wrap
     */
    private ViewPumpContextWrapper(Context base) {
        super(base);
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = new ViewPumpLayoutInflater(LayoutInflater.from(getBaseContext()), this, false);
            }
            return mInflater;
        }
        return super.getSystemService(name);
    }
}
