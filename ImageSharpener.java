import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * ImageSharpener
 * ----------------------------
 * Provides static methods to sharpen an image using a customizable kernel.
 * The sharpen strength can be adjusted with a float parameter (1.0 = normal).
 */
public class ImageSharpener {

    /**
     * Sharpens the given image using a 3x3 kernel with adjustable strength.
     *
     * @param img The input BufferedImage.
     * @param strength The sharpening strength multiplier (1.0 = normal, 0.5 = mild, 2.0 = strong).
     * @return A new BufferedImage that has been sharpened.
     */
    public static BufferedImage sharpen(BufferedImage img, float strength) {
        if (img == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        // Ensure valid range for strength
        strength = Math.max(0.1f, Math.min(strength, 5.0f));

        // Define kernel with adjustable intensity
        float center = 5f * strength;
        float edge = -1f * strength;

        float[] kernel = {
            0f, edge, 0f,
            edge, center, edge,
            0f, edge, 0f
        };

        Kernel k = new Kernel(3, 3, kernel);
        ConvolveOp op = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);

        // Apply the filter and return new image
        BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        op.filter(img, dest);
        return dest;
    }

    /**
     * Convenience method for standard sharpening (strength = 1.0f).
     *
     * @param img The input BufferedImage.
     * @return A new sharpened image.
     */
    public static BufferedImage sharpen(BufferedImage img) {
        return sharpen(img, 1.0f);
    }
}
