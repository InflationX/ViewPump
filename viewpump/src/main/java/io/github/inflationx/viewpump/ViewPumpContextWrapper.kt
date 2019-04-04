package io.github.inflationx.viewpump

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import io.github.inflationx.viewpump.internal.`-ViewPumpActivityFactory`
import io.github.inflationx.viewpump.internal.`-ViewPumpLayoutInflater`
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Uses the default configuration from [ViewPump]
 *
 * Remember if you are defining default in the
 * [ViewPump] make sure this is initialised before
 * the activity is created.
 *
 * @param base ContextBase to Wrap
 */
class ViewPumpContextWrapper private constructor(base: Context) : ContextWrapper(base) {

  private val inflater: `-ViewPumpLayoutInflater` by lazy(NONE) {
    `-ViewPumpLayoutInflater`(
        LayoutInflater.from(baseContext), this, false)
  }

  override fun getSystemService(name: String): Any? {
    if (Context.LAYOUT_INFLATER_SERVICE == name) {
      return inflater
    }
    return super.getSystemService(name)
  }

  companion object {

    /**
     * Uses the default configuration from [ViewPump]
     *
     * Remember if you are defining default in the [ViewPump] make sure this
     * is initialised before the activity is created.
     *
     * @param base ContextBase to Wrap.
     * @return ContextWrapper to pass back to the activity.
     */
    @JvmStatic
    fun wrap(base: Context): ContextWrapper {
      return ViewPumpContextWrapper(base)
    }

    /**
     * You only need to call this **IF** you disable
     * [ViewPump.Builder.setPrivateFactoryInjectionEnabled]
     * This will need to be called from the
     * [Activity.onCreateView]
     * method to enable view font injection if the view is created inside the activity onCreateView.
     *
     * You would implement this method like so in you base activity.
     *
     * ```
     * @Override
     * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
     *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs);
     * }
     * ```
     *
     * ```
     * override fun onCreateView(parent: View, name: String, context: Context, attrs: AttributeSet): View {
     *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs)
     * }
     * ```
     *
     * @param activity The activity the original that the ContextWrapper was attached too.
     * @param parent   Parent view from onCreateView
     * @param view     The View Created inside onCreateView or from super.onCreateView
     * @param name     The View name from onCreateView
     * @param context  The context from onCreateView
     * @param attr     The AttributeSet from onCreateView
     * @return The same view passed in, or null if null passed in.
     */
    @JvmStatic
    fun onActivityCreateView(activity: Activity, parent: View?, view: View, name: String,
        context: Context, attr: AttributeSet?): View? {
      return get(activity).onActivityCreateView(parent, view, name, context, attr)
    }

    /**
     * Get the ViewPump Activity Fragment Instance to allow callbacks for when views are created.
     *
     * @param activity The activity the original that the ContextWrapper was attached too.
     * @return Interface allowing you to call onActivityViewCreated
     */
    @JvmStatic
    internal fun get(activity: Activity): `-ViewPumpActivityFactory` {
      if (activity.layoutInflater !is `-ViewPumpLayoutInflater`) {
        throw RuntimeException(
            "This activity does not wrap the Base Context! See ViewPumpContextWrapper.wrap(Context)")
      }
      return activity.layoutInflater as `-ViewPumpActivityFactory`
    }
  }
}
