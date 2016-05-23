package com.csviri.security.filter.bodylength;

class InvalidContentLengthException extends RuntimeException {

	public InvalidContentLengthException(long maxLength, long actualLength) {
		super("Actual content length is larger than maximum allowed. Actual: " +
				actualLength + ", Max: " + maxLength);

	}

}
