package com.app.util;

import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;

public class JWTUtil {

    private static final String SECRET_KEY = Dotenv.load().get("JWT_SECRET");
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1 hour

    public static String generateToken(int userId, String role) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(algorithm);
    }

    // ✅ SINGLE verification point
    public static DecodedJWT validateToken(String token) {
        try {
            return JWT.require(algorithm).build().verify(token);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getUserId(DecodedJWT jwt) {
        return Integer.parseInt(jwt.getSubject());
    }

    public static String getRole(DecodedJWT jwt) {
        return jwt.getClaim("role").asString();
    }

    public static String generateCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}
