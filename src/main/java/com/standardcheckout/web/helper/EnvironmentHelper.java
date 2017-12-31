package com.standardcheckout.web.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

public class EnvironmentHelper {

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