import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESEncryption {

    private SecretKey key;
    private final SecureRandom random = new SecureRandom();

    // Generate fresh key
    public AESEncryption() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        key = kg.generateKey();
    }

    // Create from HEX key
    public AESEncryption(String hexKey) {
        key = new SecretKeySpec(hexToBytes(hexKey), "AES");
    }

    public String getKeyHex() {
        return bytesToHex(key.getEncoded());
    }

    // Encrypt
    public byte[] encrypt(byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        random.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plain);

        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, 12);
        System.arraycopy(ct, 0, out, 12, ct.length);

        return out;
    }

    // Decrypt
    public byte[] decrypt(byte[] all) throws Exception {
        byte[] iv = new byte[12];
        System.arraycopy(all, 0, iv, 0, 12);

        byte[] ct = new byte[all.length - 12];
        System.arraycopy(all, 12, ct, 0, ct.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        return cipher.doFinal(ct);
    }

    // Utility HEX
    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            out[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i+1), 16));
        return out;
    }
}
