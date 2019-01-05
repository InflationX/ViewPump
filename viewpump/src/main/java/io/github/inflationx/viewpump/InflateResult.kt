package io.github.inflationx.viewpump

import android.content.Context
import android.util.AttributeSet
import android.view.View

data class InflateResult(
    @get:JvmName("view")
    val view: View? = null,
    @get:JvmName("name")
    val name: String,
    @get:JvmName("context")
    val context: Context,
    @get:JvmName("attrs")
    val attrs: AttributeSet? = null
) {

  fun toBuilder(): Builder {
    return Builder(this)
  }

  class Builder {
    private var view: View? = null
    private var name: String? = null
    private var context: Context? = null
    private var attrs: AttributeSet? = null

    internal constructor()

    internal constructor(result: InflateResult) {
      this.view = result.view
      this.name = result.name
      this.context = result.context
      this.attrs = result.attrs
    }

    fun view(view: View?) = apply {
      this.view = view
    }

    fun name(name: String) = apply {
      this.name = name
    }

    fun context(context: Context) = apply {
      this.context = context
    }

    fun attrs(attrs: AttributeSet?) = apply {
      this.attrs = attrs
    }

    fun build(): InflateResult {
      val finalName = checkNotNull(name) { "name == null" }
      return InflateResult(
          view = view?.also {
            check(finalName == it.javaClass.name) {
              "name ($finalName) must be the view's fully qualified name (${it.javaClass.name})"
            }
          },
          name = finalName,
          context = context ?: throw IllegalStateException("context == null"),
          attrs = attrs
      )
    }
  }

  companion object {

    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
