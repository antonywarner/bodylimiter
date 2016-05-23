package com.csviri.security.filter.bodylength;

import java.io.IOException;

import javax.servlet.ServletInputStream;

class CountingInputStream extends ServletInputStream {

	private final ServletInputStream wrappedInputStream;
	private final ContentLengthCounter contentLengthCounter;

	public CountingInputStream(ServletInputStream wrappedInputStream, long maxContentLength) {
		this.wrappedInputStream = wrappedInputStream;
		this.contentLengthCounter = new ContentLengthCounter(maxContentLength);
	}

	@Override
	public int read() throws IOException {
		int b = wrappedInputStream.read();
		if (b != -1) {
			contentLengthCounter.increaseWithIgnoreNegative(1);
		}
		return b;
	}
}
