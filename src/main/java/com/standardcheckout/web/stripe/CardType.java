package com.standardcheckout.web.stripe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.Range;

public enum CardType {

    VISA("4"),
    MASTERCARD("22-27", "50-64", "66-69"),
    AMERICAN_EXPRESS("34", "37"),
    DINERS_CLUB("30", "36", "38"),
    DISCOVER("601100-601109", "601120-601149", "601174", "601177-601179", "601186-601199", "644000-650000"),
    JCB("3528-3589");

	private static final SortedMap<Range<Integer>, CardType> RANGES = new TreeMap<>((o1, o2) -> Integer.compare(o2.getMaximum(), o1.getMaximum()));

	static {
		for (CardType card : values()) {
			card.ranges.forEach(range -> RANGES.put(range, card));
		}
	}

	public static CardType fromName(String name) {
		if (name == null) {
			return null;
		}

		try {
			return valueOf(name.replace(' ', '_').trim().toUpperCase());
		} catch (Exception thatsOk) {
			return null;
		}
	}

	public static CardType detect(String card) {
		if (card == null) {
			return null;
		}
		card = card.replaceAll("[^0-9]", "");
		if (card.isEmpty()) {
			return null;
		}
		for (Map.Entry<Range<Integer>, CardType> entry : RANGES.entrySet()) {
			Range<Integer> range = entry.getKey();

			int length = range.getMaximum().toString().length();
			if (length > card.length()) {
				continue;
			}

			Integer prefix = Integer.valueOf(card.substring(0, length));
			if (range.contains(prefix)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private final List<Range<Integer>> ranges = new ArrayList<>();

	CardType(String... numbers) {
		for (String number : numbers) {
			String[] range = number.split("-");
			if (range.length <= 1) {
				Integer value = Integer.valueOf(number);
				ranges.add(Range.is(value));
			} else {
				Integer from = Integer.valueOf(range[0]);
				Integer to = Integer.valueOf(range[1]);
				ranges.add(Range.between(from, to));
			}
		}
	}

}