package io.github.inflationx.viewpump

/**
 * Observes, modifies, and potentially short-circuits inflation requests going out and the
 * corresponding views that are inflated or returned. Typically interceptors change the name
 * of the view to be inflated, return a programmatically instantiated view, or perform actions
 * on a view after it is inflated based on its Context or AttributeSet.
 */
interface Interceptor {
  fun intercept(chain: Chain): InflateResult

  interface Chain {
    fun request(): InflateRequest

    fun proceed(request: InflateRequest): InflateResult
  }
}
