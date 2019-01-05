package io.github.inflationx.viewpump

import android.util.Log

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Not an `object` type or `internal` for legacy reasons. Java version was public and not final.
 */
open class ReflectionUtils {

  companion object {
    private val TAG = ReflectionUtils::class.java.simpleName

    internal fun Field.setValueQuietly(obj: Any, value: Any) {
      try {
        set(obj, value)
      } catch (ignored: IllegalAccessException) {
      }
    }

    @JvmStatic
    fun getMethod(clazz: Class<*>, methodName: String): Method? {
      val methods = clazz.methods
      for (method in methods) {
        if (method.name == methodName) {
          method.isAccessible = true
          return method
        }
      }
      return null
    }

    @JvmStatic
    fun invokeMethod(target: Any, method: Method?, vararg args: Any) {
      try {
        if (method == null) return
        method.invoke(target, *args)
      } catch (e: IllegalAccessException) {
        Log.d(TAG, "Can't access method using reflection", e)
      } catch (e: InvocationTargetException) {
        Log.d(TAG, "Can't invoke method using reflection", e)
      }
    }
  }
}
