package com.csviri.security.filter.bodylength;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class CountingRequestWrapper extends HttpServletRequestWrapper {

	private final long maxContentLength;

	public CountingRequestWrapper(HttpServletRequest request, long maxContentLength) {
		super(request);
		this.maxContentLength = maxContentLength;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new CountingInputStream(super.getInputStream(), maxContentLength);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		String characterEncoding = getCharacterEncoding();
		if (characterEncoding == null) {
			characterEncoding = "ISO-8859-1";
		}
		return new BufferedReader(new InputStreamReader(getInputStream(), characterEncoding));
	}
}
