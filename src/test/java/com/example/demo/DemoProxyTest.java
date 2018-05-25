package com.example.demo;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.requestpojo.ARequestBody;
import com.netflix.zuul.context.RequestContext;


import java.util.Base64;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = DemoApplication.class)
public class DemoProxyTest {

    @Autowired
    private TestRestTemplate restTemplate;

	HttpHeaders headers = new HttpHeaders();

    static ConfigurableApplicationContext aService;

    @BeforeClass
    public static void startBookService() {
    	aService = SpringApplication.run(A.class,
                "--server.port=5000");
    }

    @AfterClass
    public static void closeBookService() {
    	aService.close();
    }

    @Before
    public void setup() {
        RequestContext.testSetCurrentContext(new RequestContext());
    }

    @Test
    public void test() {
      
        ARequestBody reqBody=new ARequestBody();
        reqBody.setImage("test");
        
        System.out.println("Input Http Request Body:"+reqBody);
        
        HttpEntity<ARequestBody> entity = new HttpEntity<ARequestBody>(reqBody, headers);

        
		ResponseEntity<ARequestBody> resp = restTemplate.exchange("/api/image",
				HttpMethod.POST, entity, ARequestBody.class);
		
		
		//Verifying the response has Base64 Encode String
		//Encoding the request data and verifying with the response data
		Assert.assertEquals(new String(Base64.getEncoder().encode(reqBody.getImage().getBytes())), resp.getBody().getImage());
		
		//Verifying whatever was base64 encoded is the request sent in first place
		//Decoding the response and verifying with request data
		Assert.assertEquals(reqBody.getImage(), new String(Base64.getDecoder().decode(resp.getBody().getImage())));

    }

    @Configuration
    @EnableAutoConfiguration
    @RestController
    @RequestMapping(value="/api")
    static class A {
        @RequestMapping(value="/image", method = RequestMethod.POST)
        public ARequestBody getImage(@RequestBody ARequestBody areq) {
            return areq;
        }
    }
}
