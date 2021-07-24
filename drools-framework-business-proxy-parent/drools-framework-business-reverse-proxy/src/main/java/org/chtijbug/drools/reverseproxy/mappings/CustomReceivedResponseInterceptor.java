package org.chtijbug.drools.reverseproxy.mappings;

import com.github.mkopylec.charon.configuration.MappingProperties;
import com.github.mkopylec.charon.core.http.ReceivedResponseInterceptor;
import com.github.mkopylec.charon.core.http.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomReceivedResponseInterceptor implements ReceivedResponseInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(CustomReceivedResponseInterceptor.class);
    @Override
    public void intercept(ResponseData responseData, MappingProperties mappingProperties) {
        logger.debug("data received = {}",responseData);
    }
}
