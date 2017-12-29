package com.standardcheckout.web.webstore;

public interface WebstoreService {

	Webstore getWebstore(String storeId);

	void saveWebstore(Webstore webstore);

}
