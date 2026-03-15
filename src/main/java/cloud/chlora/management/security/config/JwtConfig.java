package cloud.chlora.management.security.config;

import cloud.chlora.management.security.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        String publicKeyPem = new String(jwtProperties.getPublicKey().getInputStream().readAllBytes());
        RSAPublicKey publicKey = parsePublicKey(publicKeyPem);

        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    private RSAPublicKey parsePublicKey(String publicKeyPem) throws Exception {
        String clean = publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] bytes = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
