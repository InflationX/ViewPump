package io.github.inflationx.viewpump.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.github.inflationx.viewpump.FallbackViewCreator;
import io.github.inflationx.viewpump.InflateRequest;
import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.util.AnotherTestView;
import io.github.inflationx.viewpump.util.AnotherTestViewNewingPreInflationInterceptor;
import io.github.inflationx.viewpump.util.NameChangingPreInflationInterceptor;
import io.github.inflationx.viewpump.util.SingleConstructorTestView;
import io.github.inflationx.viewpump.util.TestFallbackViewCreator;
import io.github.inflationx.viewpump.util.TestPostInflationInterceptor;
import io.github.inflationx.viewpump.util.TestView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.STRICT_STUBS;

public class ViewPumpTest {

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().strictness(STRICT_STUBS);

    @Mock Context mockContext;
    @Mock AttributeSet mockAttrs;
    @Mock View mockParentView;

    private ViewPump testPump() {
        return ViewPump.builder().build();
    }

    /** @noinspection deprecation*/
    @Test
    public void uninitViewPump_shouldProvideDefaultInstance() {
        assertThat(ViewPump.get()).isNotNull();
    }

    /** @noinspection deprecation*/
    @Test
    public void initViewPump_shouldProvideConfiguredInstance() {
        ViewPump viewPump = ViewPump.builder().build();
        ViewPump.init(viewPump);

        assertThat(ViewPump.get())
                .isNotNull()
                .isSameAs(viewPump);
    }

    @Test
    public void request_withRequiredParams_shouldReturnView() {
        InflateResult result = testPump().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(new TestFallbackViewCreator())
                .build());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(TestView.NAME);
        assertThat(result.context()).isSameAs(mockContext);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(TestView.class);
    }

    @Test
    public void request_withAdditionalParams_shouldReturnView() {
        InflateResult result = testPump().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .parent(mockParentView)
                .fallbackViewCreator(new TestFallbackViewCreator())
                .build());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(TestView.NAME);
        assertThat(result.context()).isSameAs(mockContext);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(TestView.class);
    }

    @Test
    public void request_withInflatedNameChangeInterceptor_shouldReturnViewWithNewName() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build();

        InflateResult result = pump.inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(new TestFallbackViewCreator())
                .build());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withViewNewingInterceptor_shouldReturnViewWithoutFallingBack() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .build();

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = pump.inflate(InflateRequest.builder()
                .name(AnotherTestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyNoInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withViewNewingInterceptor_shouldShortcircuitDownstreamInterceptorsAndFallback() {
        Interceptor starvedInterceptor = mock(Interceptor.class);

        ViewPump pump = ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .addInterceptor(starvedInterceptor)
                .build();

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = pump.inflate(InflateRequest.builder()
                .name(AnotherTestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyNoInteractions(starvedInterceptor);
        verifyNoInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withNameChangingAndViewNewingInterceptorInOrder_shouldReturnViewWithNewNameWithoutFallback() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .build();

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = pump.inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyNoInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withNameChangingAndViewNewingInterceptorWrongOrder_shouldReturnViewWithNewNameWithFallback() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build();

        View fallbackView = new AnotherTestView(mockContext);
        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);
        when(mockFallbackViewCreator.onCreateView(
                    nullable(View.class),
                    eq(AnotherTestView.NAME),
                    eq(mockContext),
                    nullable(AttributeSet.class)))
                .thenReturn(fallbackView);

        InflateResult result = pump.inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .attrs(mockAttrs)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verify(mockFallbackViewCreator)
                .onCreateView(nullable(View.class), eq(AnotherTestView.NAME), eq(mockContext), nullable(AttributeSet.class));

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class)
                .isSameAs(fallbackView);
    }

    @Test
    public void createView_fromClassName_shouldReturnView() {
        View view = testPump().create(mockContext, TestView.class, mockAttrs);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(TestView.class);

        assertThat(((TestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_fromClassNameWithSingleParamConstructor_shouldReturnView() {
        View view = testPump().create(mockContext, SingleConstructorTestView.class, mockAttrs);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(SingleConstructorTestView.class);

        assertThat(((SingleConstructorTestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_withPreInflationInterceptor_shouldReturnViewWithNewName() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build();

        View view = pump.create(mockContext, TestView.class, mockAttrs);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);

        assertThat(((AnotherTestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_withPostInflationInterceptor_shouldReturnPostProcessedView() {
        ViewPump pump = ViewPump.builder()
                .addInterceptor(new TestPostInflationInterceptor())
                .build();

        View view = pump.create(mockContext, TestView.class, mockAttrs);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(TestView.class);

        assertThat(((TestView) view).isSameContextAs(mockContext)).isTrue();
        assertThat(((TestView) view).isPostProcessed()).isTrue();
    }

    /** @noinspection deprecation*/
    @Test
    public void reset() {
        ViewPump first = ViewPump.builder()
                .addInterceptor(new TestPostInflationInterceptor())
                .build();
        ViewPump.init(first);

        assertThat(ViewPump.get())
                .isSameAs(first);

        // Now reset
        ViewPump.reset();

        // Now it's cleared the previously installed one
        assertThat(ViewPump.get())
                .isNotSameAs(first);
    }
}
