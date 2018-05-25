package com.example.demo.filters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.util.StreamUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.ServletInputStreamWrapper;

public class SimpleFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return request.getRequestURL().toString().contains("/image");
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        try {
            InputStream in = ctx.getRequest().getInputStream();
            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            Map<String, Object> stringObjectMap = new GsonJsonParser().parseMap(body);
            /*
             * Modified to get the actual request body
             * Code was by default converting "1234" to Base64
             * 
             */
            //byte[] encodedBytes = Base64.getEncoder().encode("1234".getBytes());
            
            byte[] encodedBytes = Base64.getEncoder().encode(stringObjectMap.get("image").toString().getBytes());
            stringObjectMap.replace("image", new String(encodedBytes));
            
            /*
             * Modified the code since the encoded string was containing "\u003d\u003d" instead of "=="
             * in the c
             */
            //String s = new Gson().toJson(stringObjectMap);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String s = gson.toJson(stringObjectMap);
            log.info(s);
           
            final byte[] bodyContent = s.getBytes("UTF-8");
            ctx.setRequest(new HttpServletRequestWrapper(ctx.getRequest()) {
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(bodyContent);
                }

                @Override
                public int getContentLength() {
                    return bodyContent.length;
                }


            });

        } catch (IOException e) {
            log.error("ERROR!");
//            e.printStackTrace();
        }

        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }
}
