package com.csviri.security.filter.bodylength;

class ContentLengthCounter {

	private final long maxContentLength;
	private long actualLength;


	public ContentLengthCounter(long maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	/**
	 * Note that this is "stream API aware" implementation, negative numbers (mainly -1 "end of stream" will be there) are ignored
	 */
	public void increaseWithIgnoreNegative(long amount) {
		if (amount > 0) {
			actualLength += amount;
		}
		validateActualLength();
	}

	private void validateActualLength() {
		if (maxContentLength > -1 && actualLength > maxContentLength) {
			throw new InvalidContentLengthException(maxContentLength, actualLength);
		}
	}

}
