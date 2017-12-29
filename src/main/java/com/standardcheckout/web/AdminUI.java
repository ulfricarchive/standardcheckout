package com.standardcheckout.web;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.standardcheckout.web.helper.StringHelper;
import com.standardcheckout.web.stripe.StripeService;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreService;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.Position;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;

@Title("Standard Checkout")
@SpringUI(path = "admin")
public class AdminUI extends ScoUI {

	private static final Pattern ALPHA = Pattern.compile("[a-zA-Z]+");

	@Inject
	private WebstoreService webstores;

	@Inject
	private StripeService stripe;

	@Inject
	private PasswordEncoder encoder;

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);

		flowWebstore();
	}

	private void flowWebstore() {
		requestInput("webstore", "Webstore", webstoreField -> {
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

			return () -> flowPassword(trimmedValue);
		});
	}

	protected void flowPassword(String webstoreId) {
		Webstore existing = webstores.getWebstore(webstoreId);
		if (existing == null || existing.getPassword() == null) {
			requestPassword("Create a Webstore Password",
					"Passwords must be at between 8 and 128 characters in length. This should NOT be the same as your Buycraft password!",
					passwordField -> {
						String value = passwordField.getValue();
						if (!StringHelper.isInBounds(value, 8, 128)) {
							sendError(passwordField, "Passwords must be between 8 and 128 characters");
							return null;
						}

						Webstore created = new Webstore();
						created.setStoreId(webstoreId);
						created.setPassword(encoder.encode(value));
						webstores.saveWebstore(created);

						return () -> flowLoggedIn(created);
					});

			return;
		}

		if (existing.getStoreId() == null) {
			existing.setStoreId(webstoreId);
			webstores.saveWebstore(existing);
		}

		requestPassword("Webstore Password", passwordField -> {
			String value = passwordField.getValue();
			if (StringUtils.isEmpty(value)) {
				sendError(passwordField, "Your password is required");
				return null;
			}

			if (!encoder.matches(value, existing.getPassword())) {
				// TODO error, captcha?
				sendError(passwordField, "Your password is incorrect");
				return null;
			}

			return () -> flowLoggedIn(existing);
		});
	}

	protected void flowLoggedIn(Webstore webstore) {
		if (webstore.getStripeId() == null) {
			flowStripe(webstore);
			return;
		}

		// TODO verify we still have stripe authorization
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
		sendComponentMiddle(settings);
		TextField logoUrl = new TextField("Logo URL");
		sendComponentMiddle(logoUrl);
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

			click.getButton().setEnabled(false);

			webstores.saveWebstore(webstore);

			Notification notification = new Notification("Webstore saved", Notification.Type.HUMANIZED_MESSAGE);
			notification.setPosition(Position.TOP_CENTER);
			notification.setDelayMsec((int) TimeUnit.SECONDS.toMillis(3));
			notification.show(getPage());

			click.getButton().setEnabled(true);
		});
		button.setDisableOnClick(false);
	}

	private String randomState() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

}
