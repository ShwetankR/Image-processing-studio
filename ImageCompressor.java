import javax.imageio.*;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageCompressor {

    public static void saveCompressedJPG(BufferedImage img, File output, float quality) throws IOException {
        if (img == null) throw new IOException("Image is null.");

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) throw new IOException("No JPG writer found.");
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        FileImageOutputStream fios = new FileImageOutputStream(output);
        writer.setOutput(fios);
        writer.write(null, new IIOImage(img, null, null), param);

        writer.dispose();
        fios.close();
    }
}
