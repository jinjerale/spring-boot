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

package org.springframework.boot.autoconfigure.session;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link JdbcSessionDataSourceInitializer}.
 *
 * @author Stephane Nicoll
 * @author Yanming Zhou
 */
class JdbcSessionDataSourceInitializerTests {

	@Test
	void getDatabaseNameWithPlatformDoesNotTouchDataSource() {
		DataSource dataSource = mock(DataSource.class);
		JdbcSessionProperties properties = new JdbcSessionProperties();
		properties.setPlatform("test");
		JdbcSessionDataSourceInitializer initializer = new JdbcSessionDataSourceInitializer(dataSource,
				new DefaultResourceLoader(), properties);
		assertThat(initializer.getDatabaseName()).isEqualTo("test");
		then(dataSource).shouldHaveNoInteractions();
	}

}
