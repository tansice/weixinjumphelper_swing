package com.tansice.weixinjump.model;

public class RGBInfo {
	private int RValue;

	private int GValue;

	private int BValue;

	public int getRValue() {
		return RValue;
	}

	public void setRValue(int rValue) {
		RValue = rValue;
	}

	public int getGValue() {
		return GValue;
	}

	public void setGValue(int gValue) {
		GValue = gValue;
	}

	public int getBValue() {
		return BValue;
	}

	public void setBValue(int bValue) {
		BValue = bValue;
	}

	public void reset() {
		this.RValue = 0;
		this.GValue = 0;
		this.BValue = 0;
	}
}
