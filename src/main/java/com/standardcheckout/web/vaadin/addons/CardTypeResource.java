package com.standardcheckout.web.vaadin.addons;

import com.standardcheckout.web.stripe.CardType;
import com.vaadin.server.ThemeResource;

public class CardTypeResource extends ThemeResource {

	public CardTypeResource(CardType card) {
		super("cards/" + (card != null ? card.name().toLowerCase().replace("_", "") : "unknown") + ".png");
	}

}
