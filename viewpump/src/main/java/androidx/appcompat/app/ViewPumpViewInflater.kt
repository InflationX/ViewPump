package androidx.appcompat.app

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import androidx.appcompat.widget.VectorEnabledTintResources
import androidx.core.view.ViewCompat
import org.xmlpull.v1.XmlPullParser

class ViewPumpViewInflater : AppCompatViewInflater() {

  private val IS_PRE_LOLLIPOP = Build.VERSION.SDK_INT < 21

  fun createView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
    var inheritContext = false
    if (IS_PRE_LOLLIPOP) {
      inheritContext = if (attrs is XmlPullParser)
        (attrs as XmlPullParser).depth > 1
      else
        shouldInheritContext(parent as ViewParent)// If we have a XmlPullParser, we can detect where we are in the layout
      // Otherwise we have to use the old heuristic
    }

    val useVectors = VectorEnabledTintResources.shouldBeUsed()

    return createView(parent, name, context, attrs, inheritContext, IS_PRE_LOLLIPOP, true, useVectors)
  }

  private fun shouldInheritContext(parent: ViewParent?): Boolean {
    var parent: ViewParent? = parent
        ?: // The initial parent is null so just return false
        return false
    // TODO: get the window decor view somehow
    val windowDecor = null //mWindow.getDecorView()
    while (true) {
      if (parent == null) {
        // Bingo. We've hit a view which has a null parent before being terminated from
        // the loop. This is (most probably) because it's the root view in an inflation
        // call, therefore we should inherit. This works as the inflated layout is only
        // added to the hierarchy at the end of the inflate() call.
        return true
      } else if (parent === windowDecor || parent !is View
          || ViewCompat.isAttachedToWindow((parent as View?)!!)) {
        // We have either hit the window's decor view, a parent which isn't a View
        // (i.e. ViewRootImpl), or an attached view, so we know that the original parent
        // is currently added to the view hierarchy. This means that it has not be
        // inflated in the current inflate() call and we should not inherit the context.
        return false
      }
      parent = parent.getParent()
    }
  }
}
