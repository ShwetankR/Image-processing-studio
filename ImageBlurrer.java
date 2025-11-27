import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageBlurrer {

    public static BufferedImage blurRadius(BufferedImage src, int radius) {
        if (src == null) return null;
        if (radius <= 0) return copy(src);

        int size = radius * 2 + 1;
        float[] kernel = new float[size * size];
        float val = 1f / (size * size);

        for (int i = 0; i < kernel.length; i++)
            kernel[i] = val;

        ConvolveOp op = new ConvolveOp(new Kernel(size, size, kernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        op.filter(src, out);

        return out;
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        out.getGraphics().drawImage(src, 0, 0, null);
        return out;
    }
}
