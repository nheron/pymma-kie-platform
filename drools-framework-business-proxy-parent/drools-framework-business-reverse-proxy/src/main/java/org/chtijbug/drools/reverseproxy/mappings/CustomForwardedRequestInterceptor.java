package org.chtijbug.drools.reverseproxy.mappings;

import com.github.mkopylec.charon.configuration.MappingProperties;
import com.github.mkopylec.charon.core.http.ForwardedRequestInterceptor;
import com.github.mkopylec.charon.core.http.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomForwardedRequestInterceptor implements ForwardedRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(CustomForwardedRequestInterceptor.class);
    @Override
    public void intercept(RequestData data, MappingProperties mapping) {
            logger.debug("data forwarded = {}",data);
    }
}