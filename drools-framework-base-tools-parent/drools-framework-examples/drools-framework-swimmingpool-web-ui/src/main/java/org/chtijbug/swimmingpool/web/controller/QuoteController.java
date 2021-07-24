/*
 * Copyright 2014 Pymma Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.chtijbug.swimmingpool.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.chtijbug.drools.common.rest.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.training.leisure.swimmingpool.Quote;

import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * Date: 08/10/14
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
@RestController
@RequestMapping("/quote")
public class QuoteController {
    private static Logger logger = LoggerFactory.getLogger(QuoteController.class);
    @Value("${url.swimmingpool.calculate}")
    private String urlCalcul;
    private RestTemplate restTemplateKieServer = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/calculate/{containerId:.+}", method = RequestMethod.PUT)
    public Quote storeTicket(@PathVariable("containerId") String containerid,
                             @RequestBody Quote quoteRequest) {
        Quote responseMoteur=null;
        try {

            String completeurl = urlCalcul+"/";
            logger.info("url moteur reco : " + completeurl);
            ResponseEntity<Quote> response = restTemplateKieServer
                    .execute(completeurl, HttpMethod.PUT, requestCallback(quoteRequest,containerid), clientHttpResponse -> {
                        Quote extractedResponse = null;
                        if (clientHttpResponse.getBody() != null) {
                            Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";
                            extractedResponse = mapper.readValue(result, Quote.class);
                        }
                        ResponseEntity<Quote> extractedValue = new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                        return extractedValue;
                    });
            Quote reponseMoteur = null;
            if (response.getBody() != null) {
                reponseMoteur = response.getBody();
            } else {
                reponseMoteur = quoteRequest;
            }
            return reponseMoteur;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMoteur;
    }
    private RequestCallback requestCallback(final Quote updatedInstance,String token) {
        return clientHttpRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(clientHttpRequest.getBody(), updatedInstance);
            if (updatedInstance.getSessionLogging()!= null &&
                updatedInstance.getSessionLogging().length()>0){
                clientHttpRequest.getHeaders().add(
                        "transactionId",  updatedInstance.getSessionLogging());

            }
            clientHttpRequest.getHeaders().add(
                    Constants.AUTHORISATION_HEADER, token);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        };
    }
}
