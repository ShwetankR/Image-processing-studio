import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * EdgeDetector
 * - sobel: standard Sobel magnitude
 * - cannyApprox: approximate Canny using Gaussian blur + Sobel + thresholding
 */
public class EdgeDetector {

    public static BufferedImage sobel(BufferedImage src) {
        BufferedImage gray = toGray(src);
        int w = gray.getWidth(), h = gray.getHeight();
        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);

        int[] gx = {-1,0,1, -2,0,2, -1,0,1};
        int[] gy = {-1,-2,-1, 0,0,0, 1,2,1};

        for (int y=1;y<h-1;y++) {
            for (int x=1;x<w-1;x++) {
                int sx=0, sy=0, k=0;
                for (int j=-1;j<=1;j++) for (int i=-1;i<=1;i++){
                    int v = gray.getRaster().getSample(x+i,y+j,0);
                    sx += gx[k]*v; sy += gy[k]*v; k++;
                }
                int mag = (int)Math.min(255, Math.hypot(sx, sy));
                out.getRaster().setSample(x,y,0,mag);
            }
        }
        return out;
    }

    public static BufferedImage cannyApprox(BufferedImage src) {
        BufferedImage blurred = gaussianBlur(src, 1.4);
        BufferedImage sob = sobel(blurred);
        // simple thresholding and thinning (not full Canny)
        int w = sob.getWidth(), h = sob.getHeight();
        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        int max = 0;
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) max = Math.max(max, sob.getRaster().getSample(x,y,0));
        int high = (int)(max * 0.3);
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            int v = sob.getRaster().getSample(x,y,0);
            int val = (v >= high) ? 255 : 0;
            out.getRaster().setSample(x,y,0,val);
        }
        return out;
    }

    private static BufferedImage gaussianBlur(BufferedImage src, double sigma) {
        // small 5x5 Gaussian kernel approximation
        float[] kernel = {
                1, 4, 6, 4, 1,
                4,16,24,16,4,
                6,24,36,24,6,
                4,16,24,16,4,
                1, 4, 6, 4, 1
        };
        for (int i=0;i<kernel.length;i++) kernel[i] /= 256f;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage gray = toGray(src);
        java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(new java.awt.image.Kernel(5,5,kernel), java.awt.image.ConvolveOp.EDGE_NO_OP, null);
        op.filter(gray, out);
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
