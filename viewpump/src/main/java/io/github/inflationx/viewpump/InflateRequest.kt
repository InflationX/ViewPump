package io.github.inflationx.viewpump

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Open for legacy reasons, was not final in Java.
 */
open class InflateRequest private constructor(
    @get:JvmName("name")
    val name: String,
    @get:JvmName("context")
    val context: Context,
    @get:JvmName("attrs")
    val attrs: AttributeSet?,
    @get:JvmName("parent")
    val parent: View?,
    @get:JvmName("fallbackViewCreator")
    val fallbackViewCreator: FallbackViewCreator
) {

  fun toBuilder(): Builder {
    return Builder(this)
  }

  override fun toString(): String {
    return "InflateRequest{" +
        "name='" + name + '\''.toString() +
        ", context=" + context +
        ", attrs=" + attrs +
        ", parent=" + parent +
        ", fallbackViewCreator=" + fallbackViewCreator +
        '}'.toString()
  }

  class Builder {
    private var name: String? = null
    private var context: Context? = null
    private var attrs: AttributeSet? = null
    private var parent: View? = null
    private var fallbackViewCreator: FallbackViewCreator? = null

    internal constructor()

    internal constructor(request: InflateRequest) {
      this.name = request.name
      this.context = request.context
      this.attrs = request.attrs
      this.parent = request.parent
      this.fallbackViewCreator = request.fallbackViewCreator
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

    fun parent(parent: View?) = apply {
      this.parent = parent
    }

    fun fallbackViewCreator(fallbackViewCreator: FallbackViewCreator) = apply {
      this.fallbackViewCreator = fallbackViewCreator
    }

    fun build() =
        InflateRequest(name = name ?: throw IllegalStateException("name == null"),
            context = context ?: throw IllegalStateException("context == null"),
            attrs = attrs,
            parent = parent,
            fallbackViewCreator = fallbackViewCreator ?: throw IllegalStateException("fallbackViewCreator == null")
        )
  }

  companion object {

    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
