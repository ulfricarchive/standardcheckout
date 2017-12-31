package com.standardcheckout.web.jclouds.spring;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.io.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.standardcheckout.web.jclouds.BlobStoreService;

@Service
public class BlobStoreServiceImpl implements BlobStoreService {

	private BlobStoreContext context;
	private Gson gson = new Gson(); // TODO inject

	@Value("${BUCKET_NAME:standardcheckout}")
	private String containerName;

	@Value("${JCLOUDS_PROVIDER:transient}")
	private String jcloudsProvider;

	@PostConstruct
	public void createBucket() {
		System.out.println(containerName);
		context = ContextBuilder.newBuilder(jcloudsProvider)
				.build(BlobStoreContext.class);

		getBlobStore().createContainerInLocation(null, containerName);
	}

	@Override
	public void put(String key, Object value) {
		BlobStore blobStore = getBlobStore();
		Blob blob = blobStore.blobBuilder(key).payload(gson.toJson(value)).build();
		blobStore.putBlob(containerName, blob);
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		Blob blob = getBlobStore().getBlob(containerName, key);
		if (blob == null) {
			return null;
		}

		Payload payload = blob.getPayload();
		try {
			return payload == null ? null : gson.fromJson(new InputStreamReader(payload.openStream()), type);
		} catch (JsonSyntaxException | JsonIOException | IOException exception) {
			exception.printStackTrace(); // TODO proper error handling
			return null;
		}
	}

	@Override
	public BlobStore getBlobStore() {
		return context.getBlobStore();
	}

}
