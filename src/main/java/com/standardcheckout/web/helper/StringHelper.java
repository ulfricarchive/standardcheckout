package com.standardcheckout.web.helper;

public class StringHelper {

	public static boolean isInBounds(String value, int lower, int upper) {
		if (value == null) {
			return false;
		}

		int length = value.length();
		return length >= lower && length <= upper;
	}

	private StringHelper() {
	}

}
