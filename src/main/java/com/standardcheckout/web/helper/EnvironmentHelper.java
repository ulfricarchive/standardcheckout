package com.standardcheckout.web.helper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

public class EnvironmentHelper {

	public static Optional<String> getVariable(String name) {
		String value = System.getProperty(name);

		if (StringUtils.isEmpty(value)) {
			value = System.getenv(name);

			if (StringUtils.isEmpty(value)) {
				return Optional.empty();
			}
		}

		return Optional.of(value);
	}

	public static String readFileOnClassPath(ResourceLoader resources, String file) {
		try {
			return StreamUtils.copyToString(resources.getResource("classpath:" + file).getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private EnvironmentHelper() {
	}

}
