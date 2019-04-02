@file:JvmName("-ReflectionUtils")
package io.github.inflationx.viewpump.internal

import android.util.Log

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private const val TAG = "ReflectionUtils"

internal fun Field.setValueQuietly(obj: Any, value: Any) {
  try {
    set(obj, value)
  } catch (ignored: IllegalAccessException) {
  }
}

internal fun Class<*>.getAccessibleMethod(methodName: String): Method? {
  val methods = methods
  for (method in methods) {
    if (method.name == methodName) {
      method.isAccessible = true
      return method
    }
  }
  return null
}

internal fun Method?.invokeMethod(target: Any, vararg args: Any) {
  if (this == null) {
    return
  }
  try {
    invoke(target, *args)
  } catch (e: IllegalAccessException) {
    Log.d(TAG, "Can't access method using reflection", e)
  } catch (e: InvocationTargetException) {
    Log.d(TAG, "Can't invoke method using reflection", e)
  }
}
