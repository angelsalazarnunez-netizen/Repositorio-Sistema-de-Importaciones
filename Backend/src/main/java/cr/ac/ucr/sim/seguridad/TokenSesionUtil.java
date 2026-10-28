package cr.ac.ucr.sim.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class TokenSesionUtil {

    private TokenSesionUtil() {
    }

    public static String GenerarToken() {
        return UUID.randomUUID().toString();
    }

    public static String CalcularHash(String pvc_Token) {
        try {
            byte[] vlo_Hash = MessageDigest.getInstance("SHA-256")
                    .digest(pvc_Token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(vlo_Hash);
        } catch (NoSuchAlgorithmException vlo_Excepcion) {
            throw new IllegalStateException("No está disponible SHA-256.", vlo_Excepcion);
        }
    }
}
