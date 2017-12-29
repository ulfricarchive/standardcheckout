package com.standardcheckout.web.jclouds;

import org.jclouds.blobstore.BlobStore;

public interface BlobStoreService {

	BlobStore getBlobStore();

	void put(String key, Object value);

	<T> T get(String key, Class<T> type);

}
