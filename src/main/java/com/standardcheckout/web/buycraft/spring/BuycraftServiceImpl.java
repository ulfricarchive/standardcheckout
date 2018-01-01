package com.standardcheckout.web.buycraft.spring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.standardcheckout.web.buycraft.BuycraftService;
import com.ulfric.buycraft.model.Cart;
import com.ulfric.buycraft.model.Category;
import com.ulfric.buycraft.model.Item;
import com.ulfric.buycraft.model.Listing;
import com.ulfric.buycraft.model.ManualPayment;
import com.ulfric.buycraft.model.ManualPaymentItem;
import com.ulfric.buycraft.model.Package;
import com.ulfric.buycraft.model.Sale;

@Service
public class BuycraftServiceImpl implements BuycraftService {

	private static final String URL = "https://plugin.buycraft.net/";
	private final Cache<String, Listing> listings = CacheBuilder.newBuilder()
				.concurrencyLevel(5)
				.expireAfterWrite(20, TimeUnit.MINUTES)
				.build();

	private RestTemplate restTemplate = new RestTemplate();

	@Override
	public ManualPaymentPlan asManualPayments(Cart cart, String token) {
		Listing listing;
		try {
			listing = listings.get(token, () -> getListing(token));
		} catch (ExecutionException exception) {
			return null;
		}

		return asManualPayments(listing, cart);
	}

	private ManualPaymentPlan asManualPayments(Listing listing, Cart cart) {
		if (listing == null) {
			return null;
		}

		List<ManualPayment> payments = new ArrayList<>();
		BigDecimal totalCost = BigDecimal.ZERO;
		for (Item item : cart.getItems()) {
			Package packge = getPackageById(listing, item.getId());
			ManualPayment payment = new ManualPayment();

			payment.setIgn(cart.getUsername());
			payment.setPrice(getCost(packge, item.getQuantity()));
			totalCost = totalCost.add(payment.getPrice());
			ManualPaymentItem created = new ManualPaymentItem();
			created.setId(packge.getId());
			created.setOptions(Collections.emptyMap());
			payment.setPackages(Collections.singletonList(created));
			for (int x = 0; x < item.getQuantity(); x++) {
				payments.add(payment);
			}
		}
		ManualPaymentPlan plan = new ManualPaymentPlan();
		plan.setPayments(payments);
		plan.setTotalCost(totalCost);
		return plan;
	}

	private static BigDecimal getCost(Package packge, Integer quantity) {
		BigDecimal price = getCost(packge);
		return price.multiply(quantity == null || quantity.equals(0) ? BigDecimal.ONE : BigDecimal.valueOf(quantity))
				.setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal getCost(Package packge) {
		BigDecimal price = packge.getPrice();

		Sale sale = packge.getSale();
		if (BooleanUtils.isTrue(sale.getActive())) {
			price = price.subtract(sale.getDiscount());
		}

		return price;
	}

	private Package getPackageById(Listing listing, Integer packageId) {
		for (Category category : listing.getCategories()) {
			Package packge = getPackageById(category, packageId);
			if (packge != null) {
				return packge;
			}
		}
		return null;
	}

	private Package getPackageById(Category category, int id) {
		for (Package packge : category.getPackages()) {
			if (packge.getId() == id) {
				return packge;
			}
		}

		List<Category> subcategories = category.getSubcategories();
		if (subcategories != null) {
			for (Category subcategory : subcategories) {
				Package packge = getPackageById(subcategory, id);
				if (packge != null) {
					return packge;
				}
			}
		}

		return null;
	}

	private Listing getListing(String token) {
		return get(token, "listing", Listing.class);
	}

	@Override
	public List<Boolean> process(ManualPaymentPlan plan, String token) {
		return plan.getPayments().stream()
				.map(payment -> process(payment, token))
				.collect(Collectors.toList());
	}

	private boolean process(ManualPayment payment, String token) {
		return post(token, "payments", payment);
	}

	private <T> T get(String token, String endpoint, Class<T> returnType) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Buycraft-Secret", token);
		HttpEntity<Object> entity = new HttpEntity<>(null, headers);
		return restTemplate.exchange(URL + endpoint, HttpMethod.GET, entity, returnType).getBody();
	}

	private boolean post(String token, String endpoint, Object request) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Buycraft-Secret", token);
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> response = restTemplate.exchange(URL + endpoint, HttpMethod.POST, entity, String.class);
		return response.getStatusCodeValue() == 204;
	}

}
