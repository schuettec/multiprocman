package com.github.schuettec.multiprocman;

public class Chunk {

	private byte[] data;
	private int amount;

	public Chunk(byte[] data, int amount) {
		this.data = data;
		this.amount = amount;
	}

	public byte[] getData() {
		return data;
	}

	public int getAmount() {
		return amount;
	}

}
