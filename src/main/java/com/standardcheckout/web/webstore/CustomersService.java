package com.standardcheckout.web.webstore;

import java.util.UUID;

public interface CustomersService {

	MinecraftCustomer getCustomerByMojangId(UUID mojangId);

	void saveCustomer(MinecraftCustomer customer);

}
