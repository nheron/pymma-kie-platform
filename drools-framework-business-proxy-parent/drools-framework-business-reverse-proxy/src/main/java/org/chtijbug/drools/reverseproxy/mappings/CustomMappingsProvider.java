package org.chtijbug.drools.reverseproxy.mappings;

import com.github.mkopylec.charon.configuration.CharonProperties;
import com.github.mkopylec.charon.configuration.MappingProperties;
import com.github.mkopylec.charon.core.http.HttpClientProvider;
import com.github.mkopylec.charon.core.mappings.MappingsCorrector;
import com.github.mkopylec.charon.core.mappings.MappingsProvider;
import io.jsonwebtoken.Claims;
import org.chtijbug.drools.common.rest.Constants;
import org.chtijbug.drools.reverseproxy.service.JwtService;
import org.chtijbug.drools.reverseproxy.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class CustomMappingsProvider extends MappingsProvider {

    @Autowired
    private UpdateService updateService;

    @Autowired
    private JwtService jwtService;

    private Map<String,MappingProperties> mappingPropertiesMap = new HashMap<>();

    private Map<String, MappingProperties> mappingJWTPropertiesMap = new HashMap<>();

    public CustomMappingsProvider(ServerProperties server, CharonProperties charon, MappingsCorrector mappingsCorrector, HttpClientProvider httpClientProvider) {
        super(server, charon, mappingsCorrector,httpClientProvider);
    }

    @Override
    public MappingProperties resolveMapping(String originUri, HttpServletRequest request) {
        String token = request.getHeader(Constants.AUTHORISATION_HEADER);
        if (token!= null && token.length()>0){
            Claims claims = jwtService.decodeJWT(token);
            String uuid = (String)claims.get("uuid");
            Date expiration = claims.getExpiration();
            long nowMillis = System.currentTimeMillis()-1000*3600*24*(long)6;
            Date now = new Date(nowMillis);
            if (!expiration.before(now)) {
                MappingProperties result = mappingJWTPropertiesMap.get(uuid);
                if (result != null) {
                    return result;
                } else {
                    return super.resolveMapping(originUri, request);
                }
            }else{
                return super.resolveMapping(originUri, request);
            }
        }else {

            MappingProperties result = mappingPropertiesMap.get(UpdateService.removeSlach(originUri));
            if (result != null) {
                return result;
            } else {
                return super.resolveMapping(originUri, request);
            }
        }
    }

    @Override
    protected boolean shouldUpdateMappings(HttpServletRequest httpServletRequest) {
        return updateService.getToUpdate();
    }

    @Override
    protected List<MappingProperties> retrieveMappings() {
        List<MappingProperties>  paths= new ArrayList<>();
        paths.addAll(mappingPropertiesMap.values());
        paths.addAll(mappingJWTPropertiesMap.values());
        return paths;
    }

    public void setMappingPropertiesMap(Map<String, MappingProperties> mappingPropertiesMap) {
        this.mappingPropertiesMap = mappingPropertiesMap;
    }

    public void setMappingJWTPropertiesMap(Map<String, MappingProperties> mappingJWTPropertiesMap) {
        this.mappingJWTPropertiesMap = mappingJWTPropertiesMap;
    }
}
