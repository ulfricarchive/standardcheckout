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
import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.standardcheckout.web.jclouds.BlobRepository;

@Repository
public class BlobRepositoryImpl implements BlobRepository {

	private BlobStoreContext context;
	private Gson gson = new Gson(); // TODO inject

	@Value("${BUCKET_NAME:standardcheckout}")
	private String containerName;

	@Value("${JCLOUDS_PROVIDER:transient}")
	private String jcloudsProvider;

	@Value("${JCLOUDS_ID:none}")
	private String jcloudsId;

	@Value("${JCLOUDS_SECRET:}")
	private String jcloudsSecret;

	@PostConstruct
	public void createBucket() {
		context = ContextBuilder.newBuilder(jcloudsProvider)
				.credentials(jcloudsId, jcloudsSecret.replace("\\n", "\n"))
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

	public BlobStore getBlobStore() {
		return context.getBlobStore();
	}

}
