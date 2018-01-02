package com.standardcheckout.web.webstore;

public interface WebstoreService {

	Webstore getWebstore(String storeId);

	Webstore getRefreshed(Webstore webstore);

	void saveWebstore(Webstore webstore);

}
