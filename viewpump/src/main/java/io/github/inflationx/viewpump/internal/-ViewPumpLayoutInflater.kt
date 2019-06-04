@file:JvmName("-ViewPumpLayoutInflater")
package io.github.inflationx.viewpump.internal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BuildCompat
import io.github.inflationx.viewpump.FallbackViewCreator
import io.github.inflationx.viewpump.InflateRequest
import io.github.inflationx.viewpump.R.id
import io.github.inflationx.viewpump.ViewPump
import org.xmlpull.v1.XmlPullParser
import java.lang.reflect.Field

@Suppress("ClassName")
internal class `-ViewPumpLayoutInflater`(
    original: LayoutInflater,
    newContext: Context,
    cloned: Boolean
) : LayoutInflater(original, newContext), `-ViewPumpActivityFactory` {

  private val nameAndAttrsViewCreator: FallbackViewCreator = NameAndAttrsViewCreator(this)
  private val parentAndNameAndAttrsViewCreator: FallbackViewCreator = ParentAndNameAndAttrsViewCreator(this)

  // Reflection Hax
  private var setPrivateFactory = false

  private var storeLayoutResId = ViewPump.get().isStoreLayoutResId

  init {
    setUpLayoutFactories(cloned)
  }

  override fun cloneInContext(newContext: Context): LayoutInflater {
    return `-ViewPumpLayoutInflater`(this, newContext, true)
  }

  // ===
  // Wrapping goodies
  // ===

  override fun inflate(resource: Int, root: ViewGroup?, attachToRoot: Boolean): View? {
    val view = super.inflate(resource, root, attachToRoot)
    if (view != null && storeLayoutResId) {
      view.setTag(id.viewpump_layout_res, resource)
    }
    return view
  }

  override fun inflate(parser: XmlPullParser, root: ViewGroup?, attachToRoot: Boolean): View {
    setPrivateFactoryInternal()
    return super.inflate(parser, root, attachToRoot)
  }

  /**
   * We don't want to unnecessary create/set our factories if there are none there. We try to be
   * as lazy as possible.
   */
  private fun setUpLayoutFactories(cloned: Boolean) {
    if (cloned) return
    // If we are HC+ we get and set Factory2 otherwise we just wrap Factory1
    if (factory2 != null && factory2 !is WrapperFactory2) {
      // Sets both Factory/Factory2
      factory2 = factory2
    }
    // We can do this as setFactory2 is used for both methods.
    if (factory != null && factory !is WrapperFactory) {
      factory = factory
    }
  }

  override fun setFactory(factory: LayoutInflater.Factory) {
    // Only set our factory and wrap calls to the Factory trying to be set!
    if (factory !is WrapperFactory) {
      super.setFactory(
          WrapperFactory(factory))
    } else {
      super.setFactory(factory)
    }
  }

  override fun setFactory2(factory2: LayoutInflater.Factory2) {
    // Only set our factory and wrap calls to the Factory2 trying to be set!
    if (factory2 !is WrapperFactory2) {
      // LayoutInflaterCompat.setFactory(this, new WrapperFactory2(factory2, mViewPumpFactory));
      super.setFactory2(
          WrapperFactory2(factory2))
    } else {
      super.setFactory2(factory2)
    }
  }

  private fun setPrivateFactoryInternal() {
    // Already tried to set the factory.
    if (setPrivateFactory) return
    // Reflection (Or Old Device) skip.
    if (!ViewPump.get().isReflection) return
    // Skip if not attached to an activity.
    if (context !is LayoutInflater.Factory2) {
      setPrivateFactory = true
      return
    }

    // TODO: we need to get this and wrap it if something has already set this
    val setPrivateFactoryMethod = LayoutInflater::class.java.getAccessibleMethod("setPrivateFactory")

    setPrivateFactoryMethod.invokeMethod(this, PrivateWrapperFactory2(context as Factory2, this))
    setPrivateFactory = true
  }

  // ===
  // LayoutInflater ViewCreators
  // Works in order of inflation
  // ===

  /**
   * The Activity onCreateView (PrivateFactory) is the third port of call for LayoutInflation.
   * We opted to manual injection over aggressive reflection, this should be less fragile.
   */
  override fun onActivityCreateView(
      parent: View?,
      view: View,
      name: String,
      context: Context,
      attrs: AttributeSet?
  ): View? {
    return ViewPump.get()
        .inflate(InflateRequest(
            name = name,
            context = context,
            attrs = attrs,
            parent = parent,
            fallbackViewCreator = ActivityViewCreator(
                this, view)
        ))
        .view
  }

  /**
   * The LayoutInflater onCreateView is the fourth port of call for LayoutInflation.
   * BUT only for none CustomViews.
   */
  @Throws(ClassNotFoundException::class)
  override fun onCreateView(parent: View?, name: String, attrs: AttributeSet?): View? {
    return ViewPump.get()
        .inflate(InflateRequest(
            name = name,
            context = context,
            attrs = attrs,
            parent = parent,
            fallbackViewCreator = parentAndNameAndAttrsViewCreator
        ))
        .view
  }

  /**
   * The LayoutInflater onCreateView is the fourth port of call for LayoutInflation.
   * BUT only for none CustomViews.
   * Basically if this method doesn't inflate the View nothing probably will.
   */
  @Throws(ClassNotFoundException::class)
  override fun onCreateView(name: String, attrs: AttributeSet?): View? {
    return ViewPump.get()
        .inflate(InflateRequest(
            name = name,
            context = context,
            attrs = attrs,
            fallbackViewCreator = nameAndAttrsViewCreator
        ))
        .view
  }

  /**
   * Nasty method to inflate custom layouts that haven't been handled else where. If this fails it
   * will fall back through to the PhoneLayoutInflater method of inflating custom views where
   * ViewPump will NOT have a hook into.
   *
   * @param view        view if it has been inflated by this point, if this is not null this method
   * just returns this value.
   * @param name        name of the thing to inflate.
   * @param viewContext Context to inflate by if parent is null
   * @param attrs       Attr for this view which we can steal fontPath from too.
   * @return view or the View we inflate in here.
   */
  private fun createCustomViewInternal(
      view: View?,
      name: String,
      viewContext: Context,
      attrs: AttributeSet?
  ): View? {
    var mutableView = view
    // I by no means advise anyone to do this normally, but Google have locked down access to
    // the createView() method, so we never get a callback with attributes at the end of the
    // createViewFromTag chain (which would solve all this unnecessary rubbish).
    // We at the very least try to optimise this as much as possible.
    // We only call for customViews (As they are the ones that never go through onCreateView(...)).
    // We also maintain the Field reference and make it accessible which will make a pretty
    // significant difference to performance on Android 4.0+.

    // If CustomViewCreation is off skip this.
    if (!ViewPump.get().isCustomViewCreation) return mutableView
    if (mutableView == null && name.indexOf('.') > -1) {
      if (BuildCompat.isAtLeastQ()) {
        mutableView = cloneInContext(viewContext).createView(name, null, attrs)
      } else {
        @Suppress("UNCHECKED_CAST")
        val constructorArgsArr = CONSTRUCTOR_ARGS_FIELD.get(this) as Array<Any>
        val lastContext = constructorArgsArr[0]
        // The LayoutInflater actually finds out the correct context to use. We just need to set
        // it on the mConstructor for the internal method.
        // Set the constructor ars up for the createView, not sure why we can't pass these in.
        constructorArgsArr[0] = viewContext
        CONSTRUCTOR_ARGS_FIELD.setValueQuietly(this, constructorArgsArr)
        try {
          mutableView = createView(name, null, attrs)
        } catch (ignored: ClassNotFoundException) {
        } finally {
          constructorArgsArr[0] = lastContext
          CONSTRUCTOR_ARGS_FIELD.setValueQuietly(this, constructorArgsArr)
        }
      }
    }
    return mutableView
  }

  private fun superOnCreateView(parent: View?, name: String, attrs: AttributeSet?): View? {
    return try {
      super.onCreateView(parent, name, attrs)
    } catch (e: ClassNotFoundException) {
      null
    }
  }

  private fun superOnCreateView(name: String, attrs: AttributeSet?): View? {
    return try {
      super.onCreateView(name, attrs)
    } catch (e: ClassNotFoundException) {
      null
    }
  }

  // ===
  // View creators
  // ===

  private class ActivityViewCreator(
      private val inflater: `-ViewPumpLayoutInflater`,
      private val view: View
  ) : FallbackViewCreator {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return inflater.createCustomViewInternal(view, name, context, attrs)
    }
  }

  private class ParentAndNameAndAttrsViewCreator(
      private val inflater: `-ViewPumpLayoutInflater`) : FallbackViewCreator {

    override fun onCreateView(parent: View?, name: String, context: Context,
        attrs: AttributeSet?): View? {
      return inflater.superOnCreateView(parent, name, attrs)
    }
  }

  private class NameAndAttrsViewCreator(
      private val inflater: `-ViewPumpLayoutInflater`
  ) : FallbackViewCreator {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      // This mimics the {@code PhoneLayoutInflater} in the way it tries to inflate the base
      // classes, if this fails its pretty certain the app will fail at this point.
      var view: View? = null
      for (prefix in CLASS_PREFIX_LIST) {
        try {
          view = inflater.createView(name, prefix, attrs)
          if (view != null) {
            break
          }
        } catch (ignored: ClassNotFoundException) {
        }
      }
      // In this case we want to let the base class take a crack
      // at it.
      if (view == null) view = inflater.superOnCreateView(name, attrs)
      return view
    }
  }

  // ===
  // Wrapper Factories
  // ===

  /**
   * Factory 1 is the first port of call for LayoutInflation
   */
  private class WrapperFactory(factory: LayoutInflater.Factory) : LayoutInflater.Factory {

    private val viewCreator: FallbackViewCreator = WrapperFactoryViewCreator(factory)

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet?): View? {
      return ViewPump.get()
          .inflate(InflateRequest(
              name = name,
              context = context,
              attrs = attrs,
              fallbackViewCreator = viewCreator
          ))
          .view
    }
  }

  private class WrapperFactoryViewCreator(
      private val factory: LayoutInflater.Factory
  ) : FallbackViewCreator {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return factory.onCreateView(name, context, attrs)
    }
  }

  /**
   * Factory 2 is the second port of call for LayoutInflation
   */
  private open class WrapperFactory2(factory2: LayoutInflater.Factory2) : LayoutInflater.Factory2 {
    private val viewCreator = WrapperFactory2ViewCreator(factory2)

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet?): View? {
      return onCreateView(null, name, context, attrs)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return ViewPump.get()
          .inflate(InflateRequest(
              name = name,
              context = context,
              attrs = attrs,
              parent = parent,
              fallbackViewCreator = viewCreator
          ))
          .view
    }
  }

  private open class WrapperFactory2ViewCreator(
      protected val factory2: LayoutInflater.Factory2) : FallbackViewCreator {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return factory2.onCreateView(parent, name, context, attrs)
    }
  }

  /**
   * Private factory is step three for Activity Inflation, this is what is attached to the Activity
   */
  private class PrivateWrapperFactory2(
      factory2: LayoutInflater.Factory2,
      inflater: `-ViewPumpLayoutInflater`
  ) : WrapperFactory2(factory2) {

    private val viewCreator = PrivateWrapperFactory2ViewCreator(factory2, inflater)

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return ViewPump.get()
          .inflate(InflateRequest(
              name = name,
              context = context,
              attrs = attrs,
              parent = parent,
              fallbackViewCreator = viewCreator
          ))
          .view
    }
  }

  private class PrivateWrapperFactory2ViewCreator(
      factory2: LayoutInflater.Factory2,
      private val inflater: `-ViewPumpLayoutInflater`
  ) : WrapperFactory2ViewCreator(factory2), FallbackViewCreator {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet?
    ): View? {
      return inflater.createCustomViewInternal(
          factory2.onCreateView(parent, name, context, attrs), name, context, attrs)
    }
  }

  companion object {
    private val CLASS_PREFIX_LIST = setOf("android.widget.", "android.webkit.")
    private val CONSTRUCTOR_ARGS_FIELD: Field by lazy {
      requireNotNull(LayoutInflater::class.java.getDeclaredField("mConstructorArgs")) {
        "No constructor arguments field found in LayoutInflater!"
      }.apply { isAccessible = true }
    }
  }

}
