@file:JvmName("-FallbackViewCreationInterceptor")
package io.github.inflationx.viewpump

internal class FallbackViewCreationInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): InflateResult {
    val request = chain.request()
    val viewCreator = request.fallbackViewCreator()
    val fallbackView = viewCreator.onCreateView(request.parent(), request.name(), request.context(),
        request.attrs())

    return InflateResult.builder()
        .view(fallbackView)
        .name(fallbackView?.javaClass?.name ?: request.name())
        .context(request.context())
        .attrs(request.attrs())
        .build()
  }
}
