package org.chtijbug.drools.reverseproxy.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

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
