package org.example;

import au.com.dius.pact.consumer.MockServer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "dateProvider", port = "1234")
@PactFolder("E:\\Learning\\Java-Learning\\Projects\\Pact-Tests\\pacts")
public class ConsumerPactTest {

    @Pact(consumer = "ageConsumer")
    public RequestResponsePact validDateFromProvider(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        return builder
                .given("valid date received from provider")
                .uponReceiving("valid date from provider")
                .method("GET")
                .queryMatchingDate("date", "2001-02-03")
                .path("/provider/validDate")
                .willRespondWith()
                .headers(headers)
                .status(200)
                .body(LambdaDsl.newJsonBody((object) -> {
                    object.numberType("year", 2000);
                    object.numberType("month", 8);
                    object.numberType("day", 3);
                    object.booleanType("isValidDate", true);
                }).build())
                .toPact();
    }

    @Pact(consumer = "ageConsumer")
    public RequestResponsePact sayHelloProvider(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/plain");
        return builder
                .given("valid date given by user")
                .uponReceiving("hello message from provider")
                .method("GET")
                .queryMatchingDate("date", "2001-02-03")
                .path("/provider/sayHello")
                .willRespondWith()
                .headers(headers)
                .status(200)
                .body("{name: 'Hello'}")
                .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "validDateFromProvider")
    public void testValidDateFromProvider(MockServer mockServer) throws IOException {
        HttpResponse httpResponse = Request.Get(mockServer.getUrl() + "/provider/validDate?date=2001-02-03")
                .execute().returnResponse();
        Assertions.assertTrue(httpResponse.getStatusLine().getStatusCode() == 200);
        Assertions.assertTrue(JsonPath.read(httpResponse.getEntity().getContent(), "$.isValidDate").toString().equals("true"));
    }

    @Test
    @PactTestFor(pactMethod = "sayHelloProvider")
    public void testValidateHelloConsumer(MockServer mockServer) throws IOException {
        HttpResponse httpResponse = Request.Get(mockServer.getUrl() + "/provider/sayHello?date=2001-02-03")
                .execute().returnResponse();
        System.out.println("Status for say Hello :: " + httpResponse.getStatusLine().getStatusCode());
        Assertions.assertTrue(httpResponse.getStatusLine().getStatusCode() == 200);
        Assertions.assertTrue(new String(httpResponse.getEntity().getContent().readAllBytes()).equals("{name: 'Hello'}"));
    }
}
