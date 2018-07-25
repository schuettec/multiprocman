package com.github.schuettec.multiprocman;

import java.text.DecimalFormat;

/**
 * Class that manages a monitoring information for {@link ProcessController}s.
 */
public class Statistics {

	private long overallOutputAmount;

	public Statistics() {
		super();
		clear();
	}

	public void clear() {
		this.overallOutputAmount = 0;
	}

	public String overallOutbutAmountPresentable() {
		return size(this.overallOutputAmount);
	}

	public long overallOutbutAmount() {
		return this.overallOutputAmount;
	}

	/**
	 * Reports a std/in/err read amount.
	 */
	protected void reportOutputAmount(long readAmount) {
		overallOutputAmount += readAmount;
	}

	public static String size(long bytes) {
		String hrSize = "";
		if (bytes == 0) {
			return "0 bytes";
		}
		double k = bytes / 1024.0;
		double m = k / 1024.0;
		double g = m / 1024.0;
		double t = g / 1024.0;
		DecimalFormat dec = new DecimalFormat("0.00");
		if (k <= 1) {
			hrSize = bytes + " bytes";
		}
		if (k > 1) {
			hrSize = dec.format(k)
			    .concat("KB");
		}
		if (m > 1) {
			hrSize = dec.format(m)
			    .concat("MB");
		}
		if (g > 1) {
			hrSize = dec.format(g)
			    .concat("GB");
		}
		if (t > 1) {
			hrSize = dec.format(t)
			    .concat("TB");
		}
		return hrSize;
	}

}
