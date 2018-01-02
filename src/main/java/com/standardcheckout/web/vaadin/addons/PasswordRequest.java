package com.standardcheckout.web.vaadin.addons;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.standardcheckout.web.security.PasswordProtected;

public class PasswordRequest {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String title;
		private String hint;
		private Supplier<PasswordProtected> account;
		private BiConsumer<PasswordProtected, Boolean> callback;
		private boolean showReset;

		Builder() {
		}

		public PasswordRequest build() {
			Objects.requireNonNull(title, "title");
			Objects.requireNonNull(account, "account");
			Objects.requireNonNull(callback, "callback");
			return new PasswordRequest(title, hint, account, callback, showReset);
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder hint(String hint) {
			this.hint = hint;
			return this;
		}

		public Builder account(Supplier<PasswordProtected> account) {
			this.account = account;
			return this;
		}

		public Builder callback(BiConsumer<PasswordProtected, Boolean> callback) {
			this.callback = callback;
			return this;
		}

		public Builder showReset(boolean showReset) {
			this.showReset = showReset;
			return this;
		}
	}

	private PasswordRequest(String title, String hint, Supplier<PasswordProtected> account,
			BiConsumer<PasswordProtected, Boolean> callback, boolean showReset) {
		this.title = title;
		this.hint = hint;
		this.account = account;
		this.callback = callback;
		this.showReset = showReset;
	}

	private final String title;
	private final String hint;
	private final Supplier<PasswordProtected> account;
	private final BiConsumer<PasswordProtected, Boolean> callback;
	private final boolean showReset;

	public String getTitle() {
		return title;
	}

	public String getHint() {
		return hint;
	}

	public Supplier<PasswordProtected> getAccount() {
		return account;
	}

	public BiConsumer<PasswordProtected, Boolean> getCallback() {
		return callback;
	}

	public boolean getShowReset() {
		return showReset;
	}

}
