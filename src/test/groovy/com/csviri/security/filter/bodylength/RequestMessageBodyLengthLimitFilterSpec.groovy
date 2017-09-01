package com.csviri.security.filter.bodylength

import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestMessageBodyLengthLimitFilterSpec extends Specification {

    def 'Request size too large based on request body'() {
        given:
		def maxContentLength = 100
        def filter = new RequestMessageBodyLengthLimitFilter(maxContentLength, true)
        def request = Mock(HttpServletRequest)
		def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
		request.getMethod() >> "POST" 
		request.getHeader("Content-Length") >> maxContentLength
        request.getInputStream() >> generateByteStream(maxContentLength * 2)
        when:
        filter.doFilter(request, response, chain)
        then:
        1 * chain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse) >> { args -> args[0].getInputStream().eachByte({}) }
		1 * response.reset();
		1 * response.setStatus(413);
    }

	def 'Request rejected if Content-Length is too large'() {
		given:
		def maxContentLength = 100
		def filter = new RequestMessageBodyLengthLimitFilter(maxContentLength, true)
		def request = Mock(HttpServletRequest)
		def response = Mock(HttpServletResponse)
		def chain = Mock(FilterChain)
		request.getMethod() >> "POST"
		request.getHeader("Content-Length") >> maxContentLength * 2
		when:
		filter.doFilter(request, response, chain)
		then:
		1 * response.reset();
		1 * response.setStatus(413);
	}

	def 'Request rejected if Content-Length is null'() {
		given:
		def filter = new RequestMessageBodyLengthLimitFilter(100, true)
		def request = Mock(HttpServletRequest)
		def response = Mock(HttpServletResponse)
		def chain = Mock(FilterChain)
		request.getMethod() >> "POST"
		when:
		filter.doFilter(request, response, chain)
		then:
		1 * response.reset();
		1 * response.setStatus(411);
	}

    def 'Request size within limits passes'() {
        given:
		def maxContentLength = 100
        def filter = new RequestMessageBodyLengthLimitFilter(maxContentLength, true)
        def request = Mock(HttpServletRequest)
		def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
		request.getMethod() >> "POST"
		request.getHeader("Content-Length") >> maxContentLength
        request.getInputStream() >> generateByteStream(maxContentLength)
        when:
        filter.doFilter(request, response, chain)
        then:
        1 * chain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse)
        noExceptionThrown()
		0 * response.reset();
		0 * response.setStatus(_);
    }

    static ServletInputStream generateByteStream(int size) {

        byte[] b = new byte[size]
        new Random().nextBytes(b)

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b)

        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            int read() throws IOException {
                int test = byteArrayInputStream.read()
                return test
            }
        }

        return servletInputStream

    }
}
