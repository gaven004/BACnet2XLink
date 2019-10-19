package com.g.bacnet2xlink.model.elevator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RequestTest {

    @Test
    public void testBuildCheckRequest() {
        Request request = Request.buildCheckRequest((byte) 6);
        String s = request.toString();
        assertTrue("A5-81-06-00-00-87-5A".equalsIgnoreCase(s));
        System.out.println(s);

        request = Request.buildCheckRequest((byte) 7);
        s = request.toString();
        assertTrue("A5-81-07-00-00-88-5A".equalsIgnoreCase(s));
        System.out.println(s);
    }

    @Test
    public void testToString() {
        Request request = new Request();
        System.out.println(request.toString());
    }
}
