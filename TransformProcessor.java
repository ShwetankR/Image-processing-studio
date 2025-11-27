import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * TransformProcessor
 * - dftMagnitude: compute DFT magnitude (visual) on small downsample to keep it fast
 * - dctMagnitude: compute DCT magnitude visualization
 * - determinantResponse: compute simple 3x3 determinant "response" visualization
 *
 * Implementations are simple but useful as visualization tools.
 */
public class TransformProcessor {

    // downsample factor for speed
    private static final int MAX_SIDE = 128;

    public static BufferedImage dftMagnitude(BufferedImage src) {
        BufferedImage gray = toGrayScaled(src, MAX_SIDE);
        int w = gray.getWidth(), h = gray.getHeight();
        double[][] real = new double[w][h];
        double[][] imag = new double[w][h];

        for (int u=0; u<w; u++) {
            for (int v=0; v<h; v++) {
                double sumRe = 0, sumIm = 0;
                for (int x=0; x<w; x++) {
                    for (int y=0; y<h; y++) {
                        double val = gray.getRaster().getSample(x,y,0);
                        double angle = 2*Math.PI*((u*x)/(double)w + (v*y)/(double)h);
                        sumRe += val * Math.cos(angle);
                        sumIm -= val * Math.sin(angle);
                    }
                }
                real[u][v] = sumRe;
                imag[u][v] = sumIm;
            }
        }

        double max = 1e-9;
        double[][] mag = new double[w][h];
        for (int u=0; u<w; u++) for (int v=0; v<h; v++) {
            mag[u][v] = Math.log(1 + Math.hypot(real[u][v], imag[u][v]));
            if (mag[u][v] > max) max = mag[u][v];
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int u=0; u<w; u++) for (int v=0; v<h; v++) {
            int val = (int)( (mag[u][v]/max) * 255 );
            out.getRaster().setSample(u, v, 0, val);
        }
        return out;
    }

    public static BufferedImage dctMagnitude(BufferedImage src) {
        BufferedImage gray = toGrayScaled(src, MAX_SIDE);
        int N = gray.getWidth();
        int M = gray.getHeight();
        double[][] f = new double[N][M];
        for (int i=0;i<N;i++) for (int j=0;j<M;j++) f[i][j] = gray.getRaster().getSample(i,j,0);

        double[][] F = new double[N][M];
        double max = 1e-9;
        for (int u=0; u<N; u++) {
            for (int v=0; v<M; v++) {
                double sum = 0;
                for (int x=0; x<N; x++) for (int y=0;y<M;y++)
                    sum += f[x][y] * Math.cos(Math.PI*(2*x+1)*u/(2.0*N)) * Math.cos(Math.PI*(2*y+1)*v/(2.0*M));
                F[u][v] = Math.log(1 + Math.abs(sum));
                if (F[u][v] > max) max = F[u][v];
            }
        }

        BufferedImage out = new BufferedImage(N, M, BufferedImage.TYPE_BYTE_GRAY);
        for (int u=0; u<N; u++) for (int v=0; v<M; v++) {
            int val = (int)((F[u][v]/max)*255);
            out.getRaster().setSample(u, v, 0, val);
        }
        return out;
    }

    public static BufferedImage determinantResponse(BufferedImage src) {
        BufferedImage gray = toGrayScaled(src, 512); // moderate size
        int w = gray.getWidth(), h = gray.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y=1;y<h-1;y++) {
            for (int x=1;x<w-1;x++) {
                // 3x3 patch
                double[][] M = new double[3][3];
                for (int j=-1;j<=1;j++) for (int i=-1;i<=1;i++) M[j+1][i+1] = gray.getRaster().getSample(x+i, y+j,0);
                // treat as 3x3 matrix, compute determinant
                double det = determinant3x3(M);
                int v = (int)Math.min(255, Math.abs(det)/100.0);
                out.getRaster().setSample(x,y,0,v);
            }
        }
        return out;
    }

    private static double determinant3x3(double[][] m) {
        return m[0][0]*(m[1][1]*m[2][2]-m[1][2]*m[2][1])
             - m[0][1]*(m[1][0]*m[2][2]-m[1][2]*m[2][0])
             + m[0][2]*(m[1][0]*m[2][1]-m[1][1]*m[2][0]);
    }

    private static BufferedImage toGrayScaled(BufferedImage src, int maxSide) {
        int w = src.getWidth(), h = src.getHeight();
        double scale = Math.min(1.0, (double)maxSide / Math.max(w,h));
        int nw = (int)(w*scale), nh = (int)(h*scale);
        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return scaled;
    }
}