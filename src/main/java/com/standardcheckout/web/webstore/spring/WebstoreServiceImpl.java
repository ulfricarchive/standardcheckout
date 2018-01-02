package com.standardcheckout.web.webstore.spring;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.standardcheckout.web.jclouds.BlobStoreService;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreService;

@Service
public class WebstoreServiceImpl implements WebstoreService {

	@Inject
	private BlobStoreService blobs;

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

	@Override
	public Webstore getRefreshed(Webstore webstore) {
		Webstore refreshed = getWebstore(webstore.getStoreId());
		return refreshed == null ? webstore : refreshed;
	}

}
