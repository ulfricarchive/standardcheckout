package com.standardcheckout.web.webstore;

public interface WebstoreRepository {

	Webstore getWebstore(String storeId);

	void saveWebstore(Webstore webstore);

}
