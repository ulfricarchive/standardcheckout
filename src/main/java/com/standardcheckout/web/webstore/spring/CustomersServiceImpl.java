package com.standardcheckout.web.webstore.spring;

import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.standardcheckout.web.jclouds.BlobStoreService;
import com.standardcheckout.web.webstore.CustomersService;
import com.standardcheckout.web.webstore.MinecraftCustomer;

@Service
public class CustomersServiceImpl implements CustomersService {

	@Inject
	private BlobStoreService blobs;

	@Override
	public MinecraftCustomer getCustomerByMojangId(UUID mojangId) {
		String file = "customers/" + mojangId;
		return blobs.get(file, MinecraftCustomer.class);
	}

	@Override
	public void saveCustomer(MinecraftCustomer customer) {
		blobs.put("customers/" + customer.getMojangId(), customer);
	}

}
