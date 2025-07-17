package org.assistant.tools.text;

import org.assistant.util.StringUtils;

enum TextHandlerEnum implements TextHandler {
	CAMEL_TO_UNDERLINE {
		@Override
		public String handle(String input) {
			return StringUtils.toUnderScoreCase(input);
		}
	}, UNDERLINE_TO_CAMEL {
		@Override
		public String handle(String input) {
			return StringUtils.toCamelCase(input);
		}
	}, ALL_LOWER_CASE {
		@Override
		public String handle(String input) {
			return input.toLowerCase();
		}
	}, ALL_UPPER_CASE {
		@Override
		public String handle(String input) {
			return input.toUpperCase();
		}
	},
	;

	@Override
	public String getLabel() {
		return name();
	}
}