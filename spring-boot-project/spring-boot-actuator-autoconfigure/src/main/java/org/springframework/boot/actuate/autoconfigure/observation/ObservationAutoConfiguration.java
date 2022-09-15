/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.observation;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.observation.MeterObservationHandler;
import io.micrometer.observation.GlobalObservationConvention;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.tracing.MicrometerTracingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the Micrometer Observation API.
 *
 * @author Moritz Halbritter
 * @author Brian Clozel
 * @author Jonatan Ivanov
 * @since 3.0.0
 */
@AutoConfiguration(after = { CompositeMeterRegistryAutoConfiguration.class, MicrometerTracingAutoConfiguration.class })
@ConditionalOnClass(ObservationRegistry.class)
@EnableConfigurationProperties(ObservationProperties.class)
public class ObservationAutoConfiguration {

	@Bean
	static ObservationRegistryPostProcessor observationRegistryPostProcessor(
			ObjectProvider<ObservationRegistryCustomizer<?>> observationRegistryCustomizers,
			ObjectProvider<ObservationPredicate> observationPredicates,
			ObjectProvider<GlobalObservationConvention<?>> observationConventions,
			ObjectProvider<ObservationHandler<?>> observationHandlers,
			ObjectProvider<ObservationHandlerGrouping> observationHandlerGrouping) {
		return new ObservationRegistryPostProcessor(observationRegistryCustomizers, observationPredicates,
				observationConventions, observationHandlers, observationHandlerGrouping);
	}

	@Bean
	@ConditionalOnMissingBean
	ObservationRegistry observationRegistry() {
		return ObservationRegistry.create();
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MeterRegistry.class)
	@ConditionalOnMissingClass("io.micrometer.tracing.Tracer")
	static class OnlyMetricsConfiguration {

		@Bean
		@ConditionalOnMissingBean(MeterObservationHandler.class)
		@ConditionalOnBean(MeterRegistry.class)
		DefaultMeterObservationHandler defaultMeterObservationHandler(MeterRegistry meterRegistry) {
			return new DefaultMeterObservationHandler(meterRegistry);
		}

		@Bean
		OnlyMetricsObservationHandlerGrouping onlyMetricsObservationHandlerGrouping() {
			return new OnlyMetricsObservationHandlerGrouping();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Tracer.class)
	@ConditionalOnMissingClass("io.micrometer.core.instrument.MeterRegistry")
	static class OnlyTracingConfiguration {

		@Bean
		OnlyTracingObservationHandlerGrouping tracingObservationHandlerGrouping() {
			return new OnlyTracingObservationHandlerGrouping();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ MeterRegistry.class, Tracer.class })
	static class MetricsWithTracingConfiguration {

		@Bean
		@ConditionalOnMissingBean(MeterObservationHandler.class)
		@ConditionalOnBean({ MeterRegistry.class, Tracer.class })
		TracingAwareMeterObservationHandler<Observation.Context> tracingAwareMeterObservationHandler(
				MeterRegistry meterRegistry, Tracer tracer) {
			return new TracingAwareMeterObservationHandler<>(new DefaultMeterObservationHandler(meterRegistry), tracer);
		}

		@Bean
		@ConditionalOnMissingBean({ MeterObservationHandler.class, Tracer.class })
		@ConditionalOnBean(MeterRegistry.class)
		DefaultMeterObservationHandler defaultMeterObservationHandler(MeterRegistry meterRegistry) {
			return new DefaultMeterObservationHandler(meterRegistry);
		}

		@Bean
		MetricsAndTracingObservationHandlerGrouping metricsAndTracingObservationHandlerGrouping() {
			return new MetricsAndTracingObservationHandlerGrouping();
		}

	}

}
