@file:JvmName("-ViewPumpActivityFactory")
package io.github.inflationx.viewpump.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View

@Suppress("ClassName")
internal interface `-ViewPumpActivityFactory` {

  /**
   * Used to Wrap the Activity onCreateView method.
   *
   * You implement this method like so in you base activity.
   *
   * ```
   * @Override
   * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
   *   return ViewPumpContextWrapper.get(getBaseContext()).onActivityCreateView(super.onCreateView(parent, name, context, attrs), attrs);
   * }
   * ```
   *
   * ```
   * override fun onCreateView(parent: View, name: String, context: Context, attrs: AttributeSet): View {
   *   return ViewPumpContextWrapper.get(getBaseContext()).onActivityCreateView(super.onCreateView(parent, name, context, attrs), attrs)
   * }
   * ```
   *
   * @param parent  parent view, can be null.
   * @param view    result of `super.onCreateView(parent, name, context, attrs)`, this might be null, which is fine.
   * @param name    Name of View we are trying to inflate
   * @param context current context (normally the Activity's)
   * @param attrs   see [android.view.LayoutInflater.Factory2.onCreateView]  @return the result from the activities `onCreateView()`
   * @return The view passed in, or null if nothing was passed in.
   * @see android.view.LayoutInflater.Factory2
   */
  fun onActivityCreateView(parent: View?, view: View, name: String, context: Context,
      attrs: AttributeSet?): View?
}
