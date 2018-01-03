package com.standardcheckout.web.webstore.spring;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import com.standardcheckout.web.jclouds.BlobRepository;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreRepository;

@Repository
public class WebstoreRepositoryImpl implements WebstoreRepository {

	@Inject
	private BlobRepository blobs;

	@Override
	public Webstore getWebstore(String storeId) {
		if (storeId == null) {
			return null;
		}

		return blobs.get("webstores/" + storeId, Webstore.class);
	}

	@Override
	public void saveWebstore(Webstore webstore) {
		blobs.put("webstores/" + webstore.getStoreId(), webstore);
	}

}
