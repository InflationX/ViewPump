@file:JvmName("-ReflectiveFallbackViewCreator")
package io.github.inflationx.viewpump

import android.content.Context
import android.util.AttributeSet
import android.view.View

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

internal class ReflectiveFallbackViewCreator : FallbackViewCreator {
  companion object {
    private val CONSTRUCTOR_SIGNATURE_1: Array<Class<out Any>> = arrayOf(Context::class.java)
    private val CONSTRUCTOR_SIGNATURE_2: Array<Class<out Any>> = arrayOf(Context::class.java, AttributeSet::class.java)
  }

  override fun onCreateView(parent: View?, name: String, context: Context,
      attrs: AttributeSet?): View? {
    try {
      val clazz = Class.forName(name).asSubclass(View::class.java)
      var constructor: Constructor<out View>
      var constructorArgs: Array<out Any?>
      try {
        constructor = clazz.getConstructor(*CONSTRUCTOR_SIGNATURE_2)
        constructorArgs = arrayOf(context, attrs)
      } catch (e: NoSuchMethodException) {
        constructor = clazz.getConstructor(*CONSTRUCTOR_SIGNATURE_1)
        constructorArgs = arrayOf(context)
      }

      constructor.isAccessible = true
      return constructor.newInstance(*constructorArgs)
    } catch (e: ClassNotFoundException) {
      e.printStackTrace()
    } catch (e: NoSuchMethodException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    } catch (e: InstantiationException) {
      e.printStackTrace()
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
    }

    return null
  }
}
