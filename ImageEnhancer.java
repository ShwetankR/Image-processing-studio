import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class ImageEnhancer {

    /**
     * Enhances an image using contrast and brightness.
     *
     * @param src Original image
     * @param contrast Factor (1.0 = normal, 1.5 = +50%)
     * @param brightness Offset (-100 to +100)
     */
    public static BufferedImage enhance(BufferedImage src, float contrast, int brightness) {
        if (src == null) return null;

        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        RescaleOp op = new RescaleOp(contrast, brightness, null);
        op.filter(src, out);

        return out;
    }
}
