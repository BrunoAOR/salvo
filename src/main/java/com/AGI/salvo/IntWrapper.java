package com.AGI.salvo;

public class IntWrapper {
	private int number;

	public IntWrapper(int initialValue) {
		number = initialValue;
	}

	public int get() {
		return number;
	}

	public IntWrapper set(int newValue) {
		number = newValue;
		return this;
	}

	public IntWrapper add(int value) {
		number += value;
		return this;
	}

	public IntWrapper subtract(int value) {
		number -= value;
		return this;
	}

	public IntWrapper multiply(int value) {
		number *= value;
		return this;
	}

	public IntWrapper divide(int value) {
		number /= value;
		return this;
	}
}
