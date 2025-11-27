import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

public class ImageDecryptor {

    /**
     * Decrypts a .enc file using a HEX key entered by user.
     *
     * @param file    Encrypted file
     * @param hexKey  Key provided by the user (hex string)
     * @return        BufferedImage after decryption
     */
    public static BufferedImage decryptFileWithKey(File file, String hexKey) throws Exception {

        // Load encrypted bytes
        byte[] encrypted = Files.readAllBytes(file.toPath());

        // Create AES object using the user-provided key
        AESEncryption aesTemp = new AESEncryption(hexKey);

        // Decrypt
        byte[] plain = aesTemp.decrypt(encrypted);

        // Convert bytes → image
        return ImageUtils.loadImage(new ByteArrayInputStream(plain));
    }
}
