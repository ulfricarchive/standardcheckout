package com.standardcheckout.web.helper;

import org.jclouds.apis.Apis;
import org.springframework.util.StringUtils;

public class StringHelper {

	public static void main(String[] args) {
		Apis.all().forEach(System.out::println);
	}

	public static boolean isInBounds(String value, int lower, int upper) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}

		int length = value.length();
		return length >= lower && length <= upper;
	}

	private StringHelper() {
	}

}
