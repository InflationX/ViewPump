package io.github.inflationx.viewpump;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ViewPumpLayoutInflater extends LayoutInflater implements ViewPumpActivityFactory {

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit."
    };

    private FallbackViewCreator nameAndAttrsViewCreator;
    private FallbackViewCreator parentAndNameAndAttrsViewCreator;

    // Reflection Hax
    private boolean mSetPrivateFactory = false;
    private Field mConstructorArgs = null;

    private boolean mStoreLayoutResId = false;

    protected ViewPumpLayoutInflater(Context context) {
        super(context);
        mStoreLayoutResId = ViewPump.get().isStoreLayoutResId();
        nameAndAttrsViewCreator = new NameAndAttrsViewCreator(this);
        parentAndNameAndAttrsViewCreator = new ParentAndNameAndAttrsViewCreator(this);
        setUpLayoutFactories(false);
    }

    protected ViewPumpLayoutInflater(LayoutInflater original, Context newContext, final boolean cloned) {
        super(original, newContext);
        mStoreLayoutResId = ViewPump.get().isStoreLayoutResId();
        nameAndAttrsViewCreator = new NameAndAttrsViewCreator(this);
        parentAndNameAndAttrsViewCreator = new ParentAndNameAndAttrsViewCreator(this);
        setUpLayoutFactories(cloned);
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new ViewPumpLayoutInflater(this, newContext, true);
    }

    // ===
    // Wrapping goodies
    // ===


    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        View view = super.inflate(resource, root, attachToRoot);
        if (view != null && mStoreLayoutResId) {
            view.setTag(R.id.viewpump_layout_res, resource);
        }
        return view;
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        setPrivateFactoryInternal();
        return super.inflate(parser, root, attachToRoot);
    }

    /**
     * We don't want to unnecessary create/set our factories if there are none there. We try to be
     * as lazy as possible.
     */
    private void setUpLayoutFactories(boolean cloned) {
        if (cloned) return;
        // If we are HC+ we get and set Factory2 otherwise we just wrap Factory1
        if (getFactory2() != null && !(getFactory2() instanceof WrapperFactory2)) {
            // Sets both Factory/Factory2
            setFactory2(getFactory2());
        }
        // We can do this as setFactory2 is used for both methods.
        if (getFactory() != null && !(getFactory() instanceof WrapperFactory)) {
            setFactory(getFactory());
        }
    }

    @Override
    public void setFactory(Factory factory) {
        // Only set our factory and wrap calls to the Factory trying to be set!
        if (!(factory instanceof WrapperFactory)) {
            super.setFactory(new WrapperFactory(factory));
        } else {
            super.setFactory(factory);
        }
    }

    @Override
    public void setFactory2(Factory2 factory2) {
        // Only set our factory and wrap calls to the Factory2 trying to be set!
        if (!(factory2 instanceof WrapperFactory2)) {
//            LayoutInflaterCompat.setFactory(this, new WrapperFactory2(factory2, mViewPumpFactory));
            super.setFactory2(new WrapperFactory2(factory2));
        } else {
            super.setFactory2(factory2);
        }
    }

    private void setPrivateFactoryInternal() {
        // Already tried to set the factory.
        if (mSetPrivateFactory) return;
        // Reflection (Or Old Device) skip.
        if (!ViewPump.get().isReflection()) return;
        // Skip if not attached to an activity.
        if (!(getContext() instanceof Factory2)) {
            mSetPrivateFactory = true;
            return;
        }

        // TODO: we need to get this and wrap it if something has already set this
        final Method setPrivateFactoryMethod = ReflectionUtils.getMethod(LayoutInflater.class, "setPrivateFactory");

        if (setPrivateFactoryMethod != null) {
            ReflectionUtils.invokeMethod(this,
                    setPrivateFactoryMethod,
                    new PrivateWrapperFactory2((Factory2) getContext(), this));
        }
        mSetPrivateFactory = true;
    }

    // ===
    // LayoutInflater ViewCreators
    // Works in order of inflation
    // ===

    /**
     * The Activity onCreateView (PrivateFactory) is the third port of call for LayoutInflation.
     * We opted to manual injection over aggressive reflection, this should be less fragile.
     */
    @Override
    public View onActivityCreateView(View parent, View view, String name, Context context, AttributeSet attrs) {
        return ViewPump.get().inflate(InflateRequest.builder()
                .name(name)
                .context(context)
                .attrs(attrs)
                .parent(parent)
                .fallbackViewCreator(new ActivityViewCreator(this, view))
                .build()).view();
    }

    /**
     * The LayoutInflater onCreateView is the fourth port of call for LayoutInflation.
     * BUT only for none CustomViews.
     */
    @Override
    protected View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        return ViewPump.get().inflate(InflateRequest.builder()
                .name(name)
                .context(getContext())
                .attrs(attrs)
                .parent(parent)
                .fallbackViewCreator(parentAndNameAndAttrsViewCreator)
                .build()).view();
    }

    /**
     * The LayoutInflater onCreateView is the fourth port of call for LayoutInflation.
     * BUT only for none CustomViews.
     * Basically if this method doesn't inflate the View nothing probably will.
     */
    @Override
    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        return ViewPump.get().inflate(InflateRequest.builder()
                .name(name)
                .context(getContext())
                .attrs(attrs)
                .fallbackViewCreator(nameAndAttrsViewCreator)
                .build()).view();
    }

    /**
     * Nasty method to inflate custom layouts that haven't been handled else where. If this fails it
     * will fall back through to the PhoneLayoutInflater method of inflating custom views where
     * ViewPump will NOT have a hook into.
     *
     * @param parent      parent view
     * @param view        view if it has been inflated by this point, if this is not null this method
     *                    just returns this value.
     * @param name        name of the thing to inflate.
     * @param viewContext Context to inflate by if parent is null
     * @param attrs       Attr for this view which we can steal fontPath from too.
     * @return view or the View we inflate in here.
     */
    private View createCustomViewInternal(View parent, View view, String name, Context viewContext, AttributeSet attrs) {
        // I by no means advise anyone to do this normally, but Google have locked down access to
        // the createView() method, so we never get a callback with attributes at the end of the
        // createViewFromTag chain (which would solve all this unnecessary rubbish).
        // We at the very least try to optimise this as much as possible.
        // We only call for customViews (As they are the ones that never go through onCreateView(...)).
        // We also maintain the Field reference and make it accessible which will make a pretty
        // significant difference to performance on Android 4.0+.

        // If CustomViewCreation is off skip this.
        if (!ViewPump.get().isCustomViewCreation()) return view;
        if (view == null && name.indexOf('.') > -1) {
            if (mConstructorArgs == null)
                mConstructorArgs = ReflectionUtils.getField(LayoutInflater.class, "mConstructorArgs");

            final Object[] mConstructorArgsArr = (Object[]) ReflectionUtils.getValue(mConstructorArgs, this);
            final Object lastContext = mConstructorArgsArr[0];
            // The LayoutInflater actually finds out the correct context to use. We just need to set
            // it on the mConstructor for the internal method.
            // Set the constructor ars up for the createView, not sure why we can't pass these in.
            mConstructorArgsArr[0] = viewContext;
            ReflectionUtils.setValue(mConstructorArgs, this, mConstructorArgsArr);
            try {
                view = createView(name, null, attrs);
            } catch (ClassNotFoundException ignored) {
            } finally {
                mConstructorArgsArr[0] = lastContext;
                ReflectionUtils.setValue(mConstructorArgs, this, mConstructorArgsArr);
            }
        }
        return view;
    }

    private View superOnCreateView(View parent, String name, AttributeSet attrs) {
        try {
            return super.onCreateView(parent, name, attrs);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private View superOnCreateView(String name, AttributeSet attrs) {
        try {
            return super.onCreateView(name, attrs);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // ===
    // View creators
    // ===

    private static class ActivityViewCreator implements FallbackViewCreator {
        private final ViewPumpLayoutInflater inflater;
        private final View view;

        public ActivityViewCreator(ViewPumpLayoutInflater inflater, View view) {
            this.inflater = inflater;
            this.view = view;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            return inflater.createCustomViewInternal(parent, view, name, context, attrs);
        }
    }

    private static class ParentAndNameAndAttrsViewCreator implements FallbackViewCreator {
        private final ViewPumpLayoutInflater inflater;

        public ParentAndNameAndAttrsViewCreator(ViewPumpLayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            return inflater.superOnCreateView(parent, name, attrs);
        }
    }

    private static class NameAndAttrsViewCreator implements FallbackViewCreator {
        private final ViewPumpLayoutInflater inflater;

        public NameAndAttrsViewCreator(ViewPumpLayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            // This mimics the {@code PhoneLayoutInflater} in the way it tries to inflate the base
            // classes, if this fails its pretty certain the app will fail at this point.
            View view = null;
            for (String prefix : sClassPrefixList) {
                try {
                    view = inflater.createView(name, prefix, attrs);
                    if (view != null) {
                        break;
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
            // In this case we want to let the base class take a crack
            // at it.
            if (view == null) view = inflater.superOnCreateView(name, attrs);
            return view;
        }
    }

    // ===
    // Wrapper Factories
    // ===

    /**
     * Factory 1 is the first port of call for LayoutInflation
     */
    private static class WrapperFactory implements Factory {

        private final FallbackViewCreator mViewCreator;

        public WrapperFactory(Factory factory) {
            mViewCreator = new WrapperFactoryViewCreator(factory);
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return ViewPump.get().inflate(InflateRequest.builder()
                    .name(name)
                    .context(context)
                    .attrs(attrs)
                    .fallbackViewCreator(mViewCreator)
                    .build()).view();
        }
    }

    private static class WrapperFactoryViewCreator implements FallbackViewCreator {
        protected final Factory mFactory;

        public WrapperFactoryViewCreator(Factory factory) {
            this.mFactory = factory;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            return mFactory.onCreateView(name, context, attrs);
        }
    }

    /**
     * Factory 2 is the second port of call for LayoutInflation
     */
    private static class WrapperFactory2 implements Factory2 {
        protected final Factory2 mFactory2;
        private final WrapperFactory2ViewCreator mViewCreator;

        public WrapperFactory2(Factory2 factory2) {
            mFactory2 = factory2;
            mViewCreator = new WrapperFactory2ViewCreator(factory2);
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return onCreateView(null, name, context, attrs);
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return ViewPump.get().inflate(InflateRequest.builder()
                    .name(name)
                    .context(context)
                    .attrs(attrs)
                    .parent(parent)
                    .fallbackViewCreator(mViewCreator)
                    .build()).view();
        }
    }

    private static class WrapperFactory2ViewCreator implements FallbackViewCreator {
        protected final Factory2 mFactory2;

        public WrapperFactory2ViewCreator(Factory2 factory2) {
            this.mFactory2 = factory2;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            return mFactory2.onCreateView(parent, name, context, attrs);
        }
    }

    /**
     * Private factory is step three for Activity Inflation, this is what is attached to the Activity
     */
    private static class PrivateWrapperFactory2 extends WrapperFactory2 {

        private final PrivateWrapperFactory2ViewCreator mViewCreator;

        public PrivateWrapperFactory2(Factory2 factory2, ViewPumpLayoutInflater inflater) {
            super(factory2);
            mViewCreator = new PrivateWrapperFactory2ViewCreator(factory2, inflater);
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return ViewPump.get().inflate(InflateRequest.builder()
                    .name(name)
                    .context(context)
                    .attrs(attrs)
                    .parent(parent)
                    .fallbackViewCreator(mViewCreator)
                    .build()).view();
        }
    }

    private static class PrivateWrapperFactory2ViewCreator extends WrapperFactory2ViewCreator implements FallbackViewCreator {
        private final ViewPumpLayoutInflater mInflater;

        public PrivateWrapperFactory2ViewCreator(Factory2 factory2, ViewPumpLayoutInflater mInflater) {
            super(factory2);
            this.mInflater = mInflater;
        }

        @Override
        public View onCreateView(View parent, @NonNull String name, @NonNull Context context, AttributeSet attrs) {
            return mInflater.createCustomViewInternal(parent,
                    mFactory2.onCreateView(parent, name, context, attrs), name, context, attrs);
        }
    }

}
