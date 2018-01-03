package com.standardcheckout.web.jclouds;

public interface BlobRepository {

	void put(String key, Object value);

	<T> T get(String key, Class<T> type);

}
