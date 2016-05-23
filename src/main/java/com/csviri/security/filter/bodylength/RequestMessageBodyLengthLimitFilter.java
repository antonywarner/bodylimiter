package com.csviri.security.filter.bodylength;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

import static com.csviri.security.filter.bodylength.RequestMessageBodyLengthLimitFilter.ALLOW_CHUNKED_TRANSFER_PARAM;
import static com.csviri.security.filter.bodylength.RequestMessageBodyLengthLimitFilter.MAX_MESSAGE_BODY_LENGTH_PARAM;


@WebFilter(filterName = "ContentLengthValidatorFilter",
		initParams = {
				@WebInitParam(name = MAX_MESSAGE_BODY_LENGTH_PARAM, value = "52428800", description = "Limit the maximal length of content. (-1 to turn off)"),
				@WebInitParam(name = ALLOW_CHUNKED_TRANSFER_PARAM, value = "false")
		})
public class RequestMessageBodyLengthLimitFilter implements Filter {

	private final static org.slf4j.Logger log = LoggerFactory.getLogger(RequestMessageBodyLengthLimitFilter.class);

	static final String ALLOW_CHUNKED_TRANSFER_PARAM = "allowChunkedTransfer";
	static final String MAX_MESSAGE_BODY_LENGTH_PARAM = "maxMessageBodyLength";

	private static final int CONTENT_LENGTH_REQUIRED_STATUS = 411;
	private static final int TOO_LARGE_STATUS = 413;

	private static final String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final String CHUNKED_ENCODING = "chunked";
	private static final List<String> METHODS_POSSIBLE_WITH_BODY =
			Collections.unmodifiableList(Arrays.asList("POST", "PUT", "OPTIONS", "DELETE", "PATCH"));

	private long maxContentLength;
	private boolean allowChunkedTransfer;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		maxContentLength = Long.parseLong(filterConfig.getInitParameter(MAX_MESSAGE_BODY_LENGTH_PARAM));
		allowChunkedTransfer = Boolean.parseBoolean(filterConfig.getInitParameter(ALLOW_CHUNKED_TRANSFER_PARAM));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (!METHODS_POSSIBLE_WITH_BODY.contains(httpRequest.getMethod())) {
			chain.doFilter(httpRequest, httpResponse);
			return;
		}
		if (isChunkedTransfer(httpRequest)) {
			handleChunkedTransfer(httpRequest, httpResponse, chain);
		} else {
			handleNonChunkedTransfer(httpRequest, httpResponse, chain);
		}
	}

	private void handleNonChunkedTransfer(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws IOException, ServletException {
		Long contentLength = getContentLength(httpRequest);
		if (contentLength == null) {
			log.debug("No Content-Length header in request. Rejecting request on: {}", httpRequest.getRequestURL());
			httpResponse.setStatus(CONTENT_LENGTH_REQUIRED_STATUS);
		} else if (isMaxLengthSet() && maxContentLength < contentLength) {
			log.info("Content too Large:{} Max:{}. Rejecting request on:{}", new Object[]{contentLength, maxContentLength, httpRequest.getRequestURL()});
			httpResponse.setStatus(TOO_LARGE_STATUS);
		} else {
			chain.doFilter(httpRequest, httpResponse);
		}
	}

	private void handleChunkedTransfer(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws ServletException {
		// With chunked transfer request don't have the content-length provided, so could be possibly longer then the max length.
		// Therefore we give a possibility to reject these requests automatically.
		if (!allowChunkedTransfer) {
			log.warn("Chunked Transfer not allowed. Rejecting request on: {}", httpRequest.getRequestURL());
			httpResponse.setStatus(CONTENT_LENGTH_REQUIRED_STATUS);
			return;
		}
		doBodyLengthValidationInForChunkedRequest(httpRequest, httpResponse, chain);
	}

	private void doBodyLengthValidationInForChunkedRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain) throws ServletException {
		try {
			CountingRequestWrapper countingRequestWrapper = new CountingRequestWrapper(httpRequest, maxContentLength);
			chain.doFilter(countingRequestWrapper, httpResponse);
		} catch (Exception e) {
			int index = ExceptionUtils.indexOfThrowable(e, InvalidContentLengthException.class);
			if (index == -1) {
				throw new ServletException(e);
			} else {
				log.warn("Invalid content length on request on url: " + httpRequest.getRequestURL(),
						ExceptionUtils.getThrowableList(e).get(index));
				httpResponse.setStatus(TOO_LARGE_STATUS);
			}
		}
	}

	private boolean isChunkedTransfer(HttpServletRequest request) {
		String transferEncoding = request.getHeader(TRANSFER_ENCODING_HEADER);
		return CHUNKED_ENCODING.equals(transferEncoding);
	}

	/**
	 * Note that this way we return Long, therefore support larger files.
	 */
	private Long getContentLength(HttpServletRequest httpRequest) {
		String contentLength = httpRequest.getHeader(CONTENT_LENGTH_HEADER);
		if (contentLength == null) {
			return null;
		}
		return Long.parseLong(contentLength);
	}

	private boolean isMaxLengthSet() {
		return maxContentLength > -1;
	}

	@Override
	public void destroy() {
	}
}
