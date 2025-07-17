package org.assistant.ui.controls;

public interface Option {

	String getLabel();

	Object getValue();

	Option deserialize(String value);
}