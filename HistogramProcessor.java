import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * HistogramProcessor
 * - generateHistogramImage: returns a visual histogram (RGB-luminance)
 * - equalize: simple grayscale histogram equalization (returns grayscale)
 * - stretch: contrast stretching (grayscale)
 */
public class HistogramProcessor {

    public static BufferedImage generateHistogramImage(BufferedImage src) {
        BufferedImage gray = toGray(src);
        int w = gray.getWidth(), h = gray.getHeight();
        int[] hist = new int[256];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) hist[gray.getRaster().getSample(x,y,0)]++;
        int max = Arrays.stream(hist).max().orElse(1);

        int hw = 512, hh = 200;
        BufferedImage out = new BufferedImage(hw, hh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,hw,hh);
        g.setColor(Color.DARK_GRAY);

        for (int i=0;i<256;i++){
            int val = hist[i];
            int barH = (int)((val/(double)max) * (hh-20));
            int x = (int)(i * (hw/256.0));
            g.fillRect(x, hh-10-barH, (int)Math.ceil(hw/256.0), barH);
        }
        g.dispose();
        return out;
    }

    public static BufferedImage equalize(BufferedImage src) {
        BufferedImage gray = toGray(src);
        int w = gray.getWidth(), h = gray.getHeight();
        int[] hist = new int[256];
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) hist[gray.getRaster().getSample(x,y,0)]++;
        int total = w*h;
        int[] cdf = new int[256];
        cdf[0] = hist[0];
        for (int i=1;i<256;i++) cdf[i] = cdf[i-1] + hist[i];
        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++){
            int v = gray.getRaster().getSample(x,y,0);
            int nv = (cdf[v]*255)/total;
            out.getRaster().setSample(x,y,0,nv);
        }
        return out;
    }

    public static BufferedImage stretch(BufferedImage src) {
        BufferedImage gray = toGray(src);
        int w = gray.getWidth(), h = gray.getHeight();
        int min=255,max=0;
        for (int y=0;y<h;y++) for (int x=0;x<w;x++){
            int v = gray.getRaster().getSample(x,y,0);
            if (v<min) min=v;
            if (v>max) max=v;
        }
        if (max==min) return gray;
        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++){
            int v = gray.getRaster().getSample(x,y,0);
            int nv = (v-min)*255/(max-min);
            out.getRaster().setSample(x,y,0,nv);
        }
        return out;
    }

    private static BufferedImage toGray(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }
}
