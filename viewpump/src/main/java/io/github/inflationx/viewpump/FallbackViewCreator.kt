package io.github.inflationx.viewpump

import android.content.Context
import android.util.AttributeSet
import android.view.View

interface FallbackViewCreator {
  fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet?): View?
}
