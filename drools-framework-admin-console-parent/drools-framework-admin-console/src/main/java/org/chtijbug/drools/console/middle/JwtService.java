package org.chtijbug.drools.console.middle;

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

    public  String createJWT(ProjectPersist projectPersist, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(projectPersist.getUuid())
                .setIssuedAt(now)
                .setSubject("api")
                .setIssuer("pymma")
                .claim("groupID",projectPersist.getGroupID())
                .claim("artifactID",projectPersist.getArtifactID())
                .claim("branch",projectPersist.getBranch())
                .claim("mainClass",projectPersist.getMainClass())
                .claim("uuid",projectPersist.getUuid())
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }


    public Claims decodeJWT(String jwt) {
        return  Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                .parseClaimsJws(jwt.replace("bearer","")).getBody();
    }

}
