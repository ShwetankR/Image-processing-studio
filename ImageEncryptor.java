import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

public class ImageEncryptor {

    /**
     * Encrypts a BufferedImage using AES-GCM and saves the encrypted
     * byte array to a .enc file.
     *
     * @param img     Image to encrypt
     * @param output  Target output file (.enc)
     * @param aes     AESEncryption instance (fresh key each time)
     */
    public static void encryptAndSave(BufferedImage img, File output, AESEncryption aes) throws Exception {

        // Convert image to PNG bytes
        byte[] bytes = ImageUtils.toBytes(img, "png", 1f);

        // AES encrypt
        byte[] encrypted = aes.encrypt(bytes);

        // Save encrypted bytes
        Files.write(output.toPath(), encrypted);
    }
}
