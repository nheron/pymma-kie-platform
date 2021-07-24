package org.chtijbug.drools.reverseproxy.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;

@Service
public class JwtService {

    @Value("${secretkey}")
    public   String secretKey;

    public Claims decodeJWT(String jwt) {
        return  Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                .parseClaimsJws(jwt.replace("bearer","")).getBody();
    }

}
