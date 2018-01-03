package com.standardcheckout.web.ui;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.util.StringUtils;

import com.standardcheckout.web.stripe.StripeService;
import com.standardcheckout.web.vaadin.addons.PasswordRequest;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreRepository;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@Title("Standard Checkout")
@SpringUI(path = "admin")
public class AdminUI extends ScoUI {

	private static final Pattern ALPHA = Pattern.compile("[a-zA-Z]+");

	@Inject
	private WebstoreRepository webstores;

	@Inject
	private StripeService stripe;

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);

		flowWebstore();
	}

	private void flowWebstore() {
		requestInput("webstore", "Webstore", 20, webstoreField -> {
			String value = webstoreField.getValue();
			if (StringUtils.isEmpty(value)) {
				sendError(webstoreField, "You must enter a webstore name");
				return null;
			}

			String trimmedValue = value.trim();
			if (StringUtils.isEmpty(trimmedValue)) {
				sendError(webstoreField, "You must enter a webstore name other than whitespace");
				return null;
			}

			if (trimmedValue.length() < 3) {
				sendError(webstoreField, "Webstore name is too short");
				return null;
			}

			if (trimmedValue.length() > 32) {
				sendError(webstoreField, "Webstore name is too long");
				return null;
			}

			if (!ALPHA.matcher(trimmedValue).matches()) {
				sendError(webstoreField, "Webstore names can only be alpha characters");
				return null;
			}

			return () -> flowPassword(trimmedValue.toLowerCase());
		});
	}

	protected void flowPassword(String webstoreId) {
		Webstore existing = webstores.getWebstore(webstoreId);
		boolean exists = existing != null && !StringUtils.isEmpty(existing.getPassword());
		requestPassword(PasswordRequest.builder()
				.title(exists ? "Webstore password" : "Create a Webstore Password")
				.hint(exists ? null : "Passwords must be at between 8 and 128 characters in length. This should NOT be the same as your Buycraft or Stripe passwords!")
				.account(() -> {
					Webstore account = webstores.getWebstore(webstoreId);
					if (account == null) {
						account = new Webstore();
						account.setStoreId(webstoreId);
					}
					return account;
				})
				.callback((account, validated) -> {
					Webstore webstore = (Webstore) account;
					if (!validated) {
						webstores.saveWebstore(webstore);
						return;
					}

					if (StringUtils.isEmpty(webstore.getStoreId())) {
						webstore.setStoreId(webstoreId);
					}

					if (webstore.getAuthorizationId() == null) {
						webstore.setAuthorizationId(UUID.randomUUID());
					}

					if (StringUtils.isEmpty(webstore.getToken())) {
						webstore.setToken(generateToken());
					}

					if (webstore.getCreated() == null) {
						webstore.setCreated(Instant.now());
					}

					webstores.saveWebstore(webstore);

					flowLoggedIn(webstore);
				})
				.build());
	}

	protected void flowLoggedIn(Webstore webstore) {
		clearCenter();

		if (webstore.getStripeId() == null) {
			flowStripe(webstore);
			return;
		}

		// TODO verify we still have stripe authorization
		flowSettings(webstore);
	}

	protected void flowStripe(Webstore webstore) {
		deleteCookie("webstore");

		String code = getOption("code");
		if (code != null) {
			String state = getOption("state");
			if (StringUtils.isEmpty(state)) {
				// TODO display an error -- that's a system failure or a malicious user
				return;
			}

			if (!state.equals(webstore.getStripeSession())) {
				// TODO display an error -- that's a system failure or a malicious user
				return;
			}

			String userId = stripe.createUserId(code);

			webstore.setStripeId(userId);
			webstore.setStripeSession(null);
			webstores.saveWebstore(webstore);
			flowSettings(webstore);
			return;
		}

		webstore.setStripeSession(randomState());
		webstores.saveWebstore(webstore);

		String connectUrl = stripe.createConnectUrl(webstore.getStripeSession());
		if (StringUtils.isEmpty(connectUrl)) {
			// TODO display an error -- that's a system failure
			return;
		}

		setCookie("webstore", webstore.getStoreId());
		getPage().open(connectUrl, null);
	}

	protected void flowSettings(Webstore webstore) {
		Label settings = new Label("Settings");
		settings.addStyleName(ValoTheme.LABEL_LARGE);

		TextField logoUrl = new TextField("Logo URL");
		logoUrl.setMaxLength(300);

		TextField friendlyName = new TextField("Friendly Name");
		friendlyName.setMaxLength(32);
		friendlyName.setPlaceholder("Pizza Craft");

		HorizontalLayout token = new HorizontalLayout();

		TextField tokenData = new TextField("Secret");
		tokenData.setWidth("100%");
		tokenData.setEnabled(false);
		tokenData.setValue(webstore.getToken());

		Button tokenButton = new Button();
		tokenButton.setWidth("100%");
		tokenButton.setIcon(VaadinIcons.REFRESH);
		tokenButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		tokenButton.addClickListener(click -> tokenData.setValue(generateToken()));

		token.addComponents(tokenData, tokenButton);
		token.setExpandRatio(tokenData, 9);
		token.setExpandRatio(tokenButton, 1.3F);
		token.setComponentAlignment(tokenButton, Alignment.BOTTOM_CENTER);

		TextArea termsOfService = new TextArea("Terms of Service");
		termsOfService.setMaxLength(8_000);

		sendComponentMiddle(settings);
		sendComponentMiddle(friendlyName);
		sendComponentMiddle(logoUrl);
		sendComponentMiddle(token);
		sendComponentMiddle(termsOfService);

		Button button = sendFriendlyButtonMiddle("Save", click -> {
			String logoUrlValue = logoUrl.getValue();
			if (!StringUtils.isEmpty(logoUrlValue)) {
				logoUrlValue = logoUrlValue.trim();
				if (!logoUrlValue.startsWith("https://")) {
					sendError(logoUrl, "Logo URL must start with https://");
					return;
				}
				webstore.setLogoUrl(StringUtils.isEmpty(logoUrlValue) ? null : logoUrlValue);
			} else {
				webstore.setLogoUrl(null);
			}

			String friendlyNameValue = friendlyName.getValue();
			if (!StringUtils.isEmpty(friendlyNameValue)) {
				friendlyNameValue = friendlyNameValue.trim();
				if (!org.apache.commons.lang3.StringUtils.isAlphanumericSpace(friendlyNameValue)) {
					sendError(logoUrl, "Friendly Name must only contain letters, numbers, and spaces");
					return;
				}
				webstore.setFriendlyName(StringUtils.isEmpty(friendlyNameValue) ? null : friendlyNameValue);
			} else {
				webstore.setLogoUrl(null);
			}

			String termsOfServiceValue = termsOfService.getValue();
			if (!StringUtils.isEmpty(termsOfServiceValue)) {
				termsOfServiceValue = termsOfServiceValue.trim();
				if (!org.apache.commons.lang3.StringUtils.isAsciiPrintable(friendlyNameValue)) {
					sendError(logoUrl, "Terms of Service must be ascii printable");
					return;
				}
				webstore.setTermsOfService(StringUtils.isEmpty(termsOfServiceValue) ? null : termsOfServiceValue);
			} else {
				webstore.setTermsOfService(null);
			}

			click.getButton().setEnabled(false);

			webstores.saveWebstore(webstore);

			sendSuccessNotice("Webstore updated");

			click.getButton().setEnabled(true);
		});
		button.setDisableOnClick(false);
	}

	private String randomState() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

	private String generateToken() {
		return (UUID.randomUUID() + "" + UUID.randomUUID()).replace("-", "");
	}

}
