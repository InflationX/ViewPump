package io.github.inflationx.viewpump;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ReflectiveFallbackViewCreator implements FallbackViewCreator {

    private static final Class<?>[] constructorSignature2 = new Class[] {
            Context.class,
            AttributeSet.class
    };

    private static final Class<?>[] constructorSignature1 = new Class[] {
            Context.class
    };

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @Nullable AttributeSet attrs) {
        try {
            Class<? extends View> clazz = Class.forName(name).asSubclass(View.class);
            Constructor<? extends View> constructor;
            Object[] constructorArgs;
            try {
                constructor = clazz.getConstructor(constructorSignature2);
                constructorArgs = new Object[]{context, attrs};
            } catch (NoSuchMethodException e) {
                constructor = clazz.getConstructor(constructorSignature1);
                constructorArgs = new Object[]{context};
            }
            constructor.setAccessible(true);
            return constructor.newInstance(constructorArgs);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
