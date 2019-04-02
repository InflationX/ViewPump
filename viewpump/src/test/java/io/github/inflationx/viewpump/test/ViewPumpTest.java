package io.github.inflationx.viewpump.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ViewPumpTest {

    @Mock Context mockContext;
    @Mock AttributeSet mockAttrs;
    @Mock View mockParentView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ViewPump.init(null);
    }

    @Test
    public void uninitViewPump_shouldProvideDefaultInstance() {
        assertThat(ViewPump.get()).isNotNull();
    }

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
        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
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
        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
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
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build());

        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
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
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .build());

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(AnotherTestView.NAME)
                .context(mockContext)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyZeroInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withViewNewingInterceptor_shouldShortcircuitDownstreamInterceptorsAndFallback() {
        Interceptor starvedInterceptor = mock(Interceptor.class);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .addInterceptor(starvedInterceptor)
                .build());

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(AnotherTestView.NAME)
                .context(mockContext)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyZeroInteractions(starvedInterceptor);
        verifyZeroInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withNameChangingAndViewNewingInterceptorInOrder_shouldReturnViewWithNewNameWithoutFallback() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .build());

        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);

        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
                .fallbackViewCreator(mockFallbackViewCreator)
                .build());

        verifyZeroInteractions(mockFallbackViewCreator);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(AnotherTestView.NAME);
        assertThat(result.view())
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);
    }

    @Test
    public void request_withNameChangingAndViewNewingInterceptorWrongOrder_shouldReturnViewWithNewNameWithFallback() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new AnotherTestViewNewingPreInflationInterceptor())
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build());

        View fallbackView = new AnotherTestView(mockContext);
        FallbackViewCreator mockFallbackViewCreator = mock(FallbackViewCreator.class);
        when(mockFallbackViewCreator.onCreateView(
                    nullable(View.class),
                    eq(AnotherTestView.NAME),
                    eq(mockContext),
                    nullable(AttributeSet.class)))
                .thenReturn(fallbackView);

        InflateResult result = ViewPump.get().inflate(InflateRequest.builder()
                .name(TestView.NAME)
                .context(mockContext)
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
        View view = ViewPump.create(mockContext, TestView.class);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(TestView.class);

        assertThat(((TestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_fromClassNameWithSingleParamConstructor_shouldReturnView() {
        View view = ViewPump.create(mockContext, SingleConstructorTestView.class);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(SingleConstructorTestView.class);

        assertThat(((SingleConstructorTestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_withPreInflationInterceptor_shouldReturnViewWithNewName() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new NameChangingPreInflationInterceptor())
                .build());

        View view = ViewPump.create(mockContext, TestView.class);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(AnotherTestView.class);

        assertThat(((AnotherTestView) view).isSameContextAs(mockContext)).isTrue();
    }

    @Test
    public void createView_withPostInflationInterceptor_shouldReturnPostProcessedView() {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new TestPostInflationInterceptor())
                .build());

        View view = ViewPump.create(mockContext, TestView.class);

        assertThat(view)
                .isNotNull()
                .isInstanceOf(TestView.class);

        assertThat(((TestView) view).isSameContextAs(mockContext)).isTrue();
        assertThat(((TestView) view).isPostProcessed()).isTrue();
    }
}
