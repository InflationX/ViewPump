package io.github.inflationx.viewpump

import android.content.Context
import android.view.View
import androidx.annotation.MainThread
import io.github.inflationx.viewpump.Interceptor.Chain
import io.github.inflationx.viewpump.ViewPump.Builder
import io.github.inflationx.viewpump.internal.`-FallbackViewCreationInterceptor`
import io.github.inflationx.viewpump.internal.`-InterceptorChain`
import io.github.inflationx.viewpump.internal.`-ReflectiveFallbackViewCreator`

class ViewPump private constructor(
    /** List of interceptors.  */
    @get:JvmName("interceptors")
    val interceptors: List<Interceptor>,

    /** Use Reflection to inject the private factory.  */
    @get:JvmName("isReflection")
    val isReflection: Boolean,

    /** Use Reflection to intercept CustomView inflation with the correct Context.  */
    @get:JvmName("isCustomViewCreation")
    val isCustomViewCreation: Boolean,

    /** Store the resourceId for the layout used to inflate the View in the View tag.  */
    @get:JvmName("isStoreLayoutResId")
    val isStoreLayoutResId: Boolean
) {

  /** List that gets cleared and reused as it holds interceptors with the fallback added.  */
  private val interceptorsWithFallback: List<Interceptor> = (interceptors + `-FallbackViewCreationInterceptor`()).toMutableList()

  fun inflate(originalRequest: InflateRequest): InflateResult {
    val chain = `-InterceptorChain`(interceptorsWithFallback, 0,
        originalRequest)
    return chain.proceed(originalRequest)
  }

  class Builder internal constructor() {

    /** List of interceptors. */
    private val interceptors = mutableListOf<Interceptor>()

    /** Use Reflection to inject the private factory. Defaults to true. */
    private var reflection = true

    /** Use Reflection to intercept CustomView inflation with the correct Context. */
    private var customViewCreation = true

    /** Store the resourceId for the layout used to inflate the View in the View tag. */
    private var storeLayoutResId = false

    /** A FallbackViewCreator used to instantiate a view via reflection when using the create() API. */
    private var reflectiveFallbackViewCreator: FallbackViewCreator? = null

    fun addInterceptor(interceptor: Interceptor) = apply {
      interceptors.add(interceptor)
    }

    /**
     *
     * Turn of the use of Reflection to inject the private factory.
     * This has operational consequences! Please read and understand before disabling.
     *
     *
     *  If you disable this you will need to override your [android.app.Activity.onCreateView]
     * as this is set as the [android.view.LayoutInflater] private factory.
     *
     * ** Use the following code in the Activity if you disable FactoryInjection:**
     *
     * ```
     * @Override
     * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
     *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs);
     * }
     * ```
     *
     * ```
     * @Override
     * override fun onCreateView(parent: View, name: String, context: Context, attrs: AttributeSet): View {
     *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs)
     * }
     * ```
     *
     * @param enabled True if private factory inject is allowed; otherwise, false.
     */
    fun setPrivateFactoryInjectionEnabled(enabled: Boolean) = apply {
      this.reflection = enabled
    }

    /**
     * Due to the poor inflation order where custom views are created and never returned inside an
     * `onCreateView(...)` method. We have to create CustomView's at the latest point in the
     * overrideable injection flow.
     *
     * On HoneyComb+ this is inside the [android.app.Activity.onCreateView]
     *
     * We wrap base implementations, so if you LayoutInflater/Factory/Activity creates the
     * custom view before we get to this point, your view is used. (Such is the case with the
     * TintEditText etc)
     *
     * The problem is, the native methods pass there parents context to the constructor in a really
     * specific place. We have to mimic this in [ViewPumpLayoutInflater.createCustomViewInternal]
     * To mimic this we have to use reflection as the Class constructor args are hidden to us.
     *
     * We have discussed other means of doing this but this is the only semi-clean way of doing it.
     * (Without having to do proxy classes etc).
     *
     * Calling this will of course speed up inflation by turning off reflection, but not by much,
     * But if you want ViewPump to inject the correct typeface then you will need to make sure your CustomView's
     * are created before reaching the LayoutInflater onViewCreated.
     *
     * @param enabled True if custom view inflated is allowed; otherwise, false.
     */
    fun setCustomViewInflationEnabled(enabled: Boolean) = apply {
      this.customViewCreation = enabled
    }

    fun setReflectiveFallbackViewCreator(
        reflectiveFallbackViewCreator: FallbackViewCreator) = apply {
      this.reflectiveFallbackViewCreator = reflectiveFallbackViewCreator
    }

    /**
     * The LayoutInflater can store the layout resourceId used to inflate a view into the inflated view's tag
     * where it can be later retrieved by an interceptor.
     *
     * @param enabled True if the view should store the resId; otherwise, false.
     */
    fun setStoreLayoutResId(enabled: Boolean) = apply {
      this.storeLayoutResId = enabled
    }

    fun build(): ViewPump {
      return ViewPump(
          interceptors = interceptors.toList(),
          isReflection = reflection,
          isCustomViewCreation = customViewCreation,
          isStoreLayoutResId = storeLayoutResId
      )
    }
  }

  companion object {

    private var INSTANCE: ViewPump? = null

    /** A FallbackViewCreator used to instantiate a view via reflection when using the create() API.  */
    private val reflectiveFallbackViewCreator: FallbackViewCreator by lazy {
      `-ReflectiveFallbackViewCreator`()
    }

    @JvmStatic
    fun init(viewPump: ViewPump?) {
      INSTANCE = viewPump
    }

    @JvmStatic
    @MainThread
    fun get(): ViewPump {
      return INSTANCE ?: builder().build().also { INSTANCE = it }
    }

    /**
     * Allows for programmatic creation of Views via reflection on class name that are still
     * pre/post-processed by the inflation interceptors.
     *
     * @param context The context.
     * @param clazz The class of View to be created.
     * @return The processed view, which might not necessarily be the same type as clazz.
     */
    @JvmStatic
    fun create(context: Context, clazz: Class<out View>): View? {
      return get()
          .inflate(InflateRequest(
              context = context,
              name = clazz.name,
              fallbackViewCreator = reflectiveFallbackViewCreator
          ))
          .view
    }

    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}

/**
 * Adds an [Interceptor] to this current [Builder] for idiomatic Kotlin higher order function usage.
 */
inline fun Builder.addInterceptor(crossinline block: (chain: Chain) -> InflateResult) = apply {
  addInterceptor(Interceptor.invoke(block))
}
