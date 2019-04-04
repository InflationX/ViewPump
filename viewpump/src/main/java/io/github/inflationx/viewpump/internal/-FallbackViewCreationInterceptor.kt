@file:JvmName("-FallbackViewCreationInterceptor")
package io.github.inflationx.viewpump.internal

import io.github.inflationx.viewpump.InflateResult
import io.github.inflationx.viewpump.Interceptor
import io.github.inflationx.viewpump.Interceptor.Chain

@Suppress("ClassName")
internal class `-FallbackViewCreationInterceptor` : Interceptor {

  override fun intercept(chain: Chain): InflateResult {
    val request = chain.request()
    val viewCreator = request.fallbackViewCreator
    val fallbackView = viewCreator.onCreateView(request.parent, request.name, request.context,
        request.attrs)

    return InflateResult(
        view = fallbackView,
        name = fallbackView?.javaClass?.name ?: request.name,
        context = request.context,
        attrs = request.attrs
    )
  }
}
