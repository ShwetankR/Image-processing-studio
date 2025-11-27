import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.FileImageOutputStream;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;

public class ImageUtils {

    /**
     * Load image from a File.
     */
    public static BufferedImage loadImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    /**
     * Load image from an InputStream.
     */
    public static BufferedImage loadImage(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    /**
     * Save image to file.
     * Automatically uses high-quality PNG or JPEG compression.
     */
    public static void saveImage(BufferedImage img, File file, String ext, float quality) throws IOException {
        ext = ext.toLowerCase();

        if (ext.equals("jpg") || ext.equals("jpeg")) {
            saveJPG(img, file, quality);
        } else {
            ImageIO.write(img, ext, file);
        }
    }

    /**
     * High-quality JPEG save.
     */
    private static void saveJPG(BufferedImage img, File file, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext())
            throw new IOException("No JPEG writer available!");

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Math.max(0.01f, Math.min(1f, quality)));

        FileImageOutputStream output = new FileImageOutputStream(file);
        writer.setOutput(output);

        writer.write(null, new IIOImage(img, null, null), param);
        writer.dispose();
        output.close();
    }

    /**
     * Convert image to compatible ARGB format.
     */
    public static BufferedImage toCompatibleImage(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;

        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImg;
    }

    /**
     * Deep copy of a BufferedImage.
     */
    public static BufferedImage deepCopy(BufferedImage src) {
        if (src == null) return null;

        ColorModel cm = src.getColorModel();
        boolean premult = cm.isAlphaPremultiplied();
        WritableRaster raster = src.copyData(null);

        return new BufferedImage(cm, raster, premult, null);
    }

    /**
     * Scale image while preserving aspect ratio.
     */
    public static BufferedImage getScaledCopy(BufferedImage src, int maxW, int maxH) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();

        double scale = Math.min((double) maxW / w, (double) maxH / h);
        scale = Math.max(0.001, scale);

        int newW = (int) (w * scale);
        int newH = (int) (h * scale);

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();

        return out;
    }

    /**
     * Convert BufferedImage to bytes (PNG encoding).
     */
    public static byte[] toBytes(BufferedImage img, String format, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        saveImage(img, new File("temp." + format), format, quality);
        return baos.toByteArray();
    }
}
