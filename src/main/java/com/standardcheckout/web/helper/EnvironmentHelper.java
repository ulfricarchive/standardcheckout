package com.standardcheckout.web.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

public class EnvironmentHelper {

	private static boolean loadedSecrets;

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

	public static Optional<String> getSecret(ResourceLoader resources, String name) {
		if (!loadedSecrets) {
			InputStream secrets = getClasspathResource(resources, "secrets.properties");
			try {
				System.getProperties().load(secrets);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return getVariable(name);
	}

	public static String readFileOnClassPath(ResourceLoader resources, String file) {
		try {
			return StreamUtils.copyToString(getClasspathResource(resources, file), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static InputStream getClasspathResource(ResourceLoader resources, String file) {
		try {
			return resources.getResource("classpath:" + file).getInputStream();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private EnvironmentHelper() {
	}

}
