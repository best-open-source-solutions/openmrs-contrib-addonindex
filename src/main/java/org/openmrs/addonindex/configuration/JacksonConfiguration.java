/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.configuration;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openmrs.addonindex.configuration.jackson.SpringHttpClientErrorExceptionMixin;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.HttpClientErrorException;

@Configuration
@JsonComponent
public class JacksonConfiguration {
	
	/**
	 * Configure Jackson to work with Java 8 dates
	 *
	 * @param builder
	 * @return
	 */
	@Bean
	@Primary
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper objectMapper = builder.createXmlMapper(false)
				.featuresToEnable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature())
				.featuresToEnable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
				.mixIn(HttpClientErrorException.class, SpringHttpClientErrorExceptionMixin.class)
				.build();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
		return objectMapper;
	}
	
}
