package com.standardcheckout.web.webstore.spring;

import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import com.standardcheckout.web.jclouds.BlobRepository;
import com.standardcheckout.web.webstore.CustomersRepository;
import com.standardcheckout.web.webstore.MinecraftCustomer;

@Repository
public class CustomersRepositoryImpl implements CustomersRepository {

	@Inject
	private BlobRepository blobs;

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
