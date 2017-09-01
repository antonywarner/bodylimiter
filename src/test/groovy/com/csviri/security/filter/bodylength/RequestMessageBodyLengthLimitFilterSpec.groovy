package com.csviri.security.filter.bodylength

import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestMessageBodyLengthLimitFilterSpec extends Specification {

    def 'Request size too large throws exception'() {
        given:
        def filter = new RequestMessageBodyLengthLimitFilter()
        def request = Mock(HttpServletRequest)
        def chain = Mock(FilterChain)
        request.getInputStream() >> generateByteStream(550)
        when:
        filter.doFilter(request, Mock(HttpServletResponse), chain)
        then:
        1 * chain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse)

    }

    def 'Request size within limits passes'() {
        given:
        def filter = new RequestMessageBodyLengthLimitFilter()
        def request = Mock(HttpServletRequest)
        def chain = Mock(FilterChain)
        request.getInputStream() >> generateByteStream(450)
        when:
        filter.doFilter(request, Mock(HttpServletResponse), chain)
        then:
        1 * chain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse)
        noExceptionThrown()
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
