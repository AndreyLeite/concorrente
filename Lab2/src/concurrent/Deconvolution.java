import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.*;

public class Deconvolution{

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Uso: java Deconvolution <imagem_borrada_path>");
            return;
        }

        String imagePath = args[0];
        BufferedImage input = ImageIO.read(new File(imagePath));
        int width = input.getWidth();
        int height = input.getHeight();

        // Separa canais R, G, B
        float[][] red = new float[height][width];
        float[][] green = new float[height][width];
        float[][] blue = new float[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(input.getRGB(x, y));
                red[y][x] = c.getRed() / 255f;
                green[y][x] = c.getGreen() / 255f;
                blue[y][x] = c.getBlue() / 255f;
            }
        }

        // Kernel Gaussiano (ajustável conforme o tipo de desfoque)
        float[][] psf = gaussianKernel(9, 2.0f);
        float[][] psfFlipped = invertKernel(psf);

        DeconRun dr1 = new DeconRun(height, red, psf, psfFlipped);
        Thread rThread = new Thread(dr1);
        rThread.start();
        
        DeconRun dr2 = new DeconRun(height, blue, psf, psfFlipped);
        Thread bThread = new Thread(dr2);
        bThread.start();
        
        DeconRun dr3 = new DeconRun(height, green, psf, psfFlipped);
        Thread gThread = new Thread(dr3);
        gThread.start();
        rThread.join();
        bThread.join();
        gThread.join();
        float [][]blueRestored = dr2.getColorRestored();
        float [][]redRestored = dr1.getColorRestored();
        float [][]greenRestored = dr3.getColorRestored();
        
        // Salva imagem restaurada
        saveColorImage(redRestored, greenRestored, blueRestored, "restaurada.png");
        System.out.println("Imagem restaurada salva como restaurada.png");
    }

    // Richardson-Lucy Iterativo
    public static float[][] richardsonLucy(float[][] image, float[][] psf, float[][] psfFlipped, int iterations) {
        int h = image.length;
        int w = image[0].length;
        float[][] estimate = new float[h][w];

        // Inicializa com valor constante (pode ser a imagem borrada)
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                estimate[y][x] = 0.5f;

        for (int it = 0; it < iterations; it++) {
            float[][] estimateBlurred = convolve(estimate, psf);
            float[][] ratio = new float[h][w];

            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    float eb = estimateBlurred[y][x];
                    ratio[y][x] = (eb > 1e-6f) ? image[y][x] / eb : 0f;
                }

            float[][] correction = convolve(ratio, psfFlipped);

            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    estimate[y][x] *= correction[y][x];
        }

        return estimate;
    }

    // Convolução 2D
    public static float[][] convolve(float[][] image, float[][] kernel) {
        int h = image.length, w = image[0].length;
        int kh = kernel.length, kw = kernel[0].length;
        int kyc = kh / 2, kxc = kw / 2;

        float[][] result = new float[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0f;
                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {
                        int iy = y + ky - kyc;
                        int ix = x + kx - kxc;
                        if (iy >= 0 && iy < h && ix >= 0 && ix < w) {
                            sum += image[iy][ix] * kernel[ky][kx];
                        }
                    }
                }
                result[y][x] = sum;
            }
        }
        return result;
    }

    // Espelha o kernel horizontal e verticalmente
    public static float[][] invertKernel(float[][] kernel) {
        int h = kernel.length, w = kernel[0].length;
        float[][] result = new float[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                result[y][x] = kernel[h - y - 1][w - x - 1];
        return result;
    }

    // Kernel Gaussiano normalizado
    public static float[][] gaussianKernel(int size, float sigma) {
        float[][] kernel = new float[size][size];
        float mean = size / 2f;
        float sum = 0f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float val = (float) Math.exp(-0.5 * (
                    Math.pow((x - mean) / sigma, 2) +
                    Math.pow((y - mean) / sigma, 2)));
                kernel[y][x] = val;
                sum += val;
            }
        }

        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                kernel[y][x] /= sum;

        return kernel;
    }

    // Salva a imagem RGB em PNG
    public static void saveColorImage(float[][] r, float[][] g, float[][] b, String filename) throws Exception {
        int h = r.length, w = r[0].length;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int red = clampToByte(r[y][x] * 255f);
                int green = clampToByte(g[y][x] * 255f);
                int blue = clampToByte(b[y][x] * 255f);
                Color color = new Color(red, green, blue);
                out.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(out, "png", new File(filename));
    }

    private static int clampToByte(float val) {
        return Math.min(255, Math.max(0, Math.round(val)));
    }

    public static class DeconRun implements Runnable{
        private int interactions;
        private float[][] color;
        private float[][] psf;
        private float[][] flip;
        private float[][] colorRestored;

        public DeconRun (int interactions, float[][] color, float[][] psf, float[][] flip){
            this.interactions = interactions;
            this.color = color;
            this.psf = psf;
            this.flip = flip;
        }

        @Override
        public void run() {
            // Aplica Richardson-Lucy por canal
            int iterations = 15;
            colorRestored = richardsonLucy(color, psf, flip, iterations);
            
        }
        public float[][] getColorRestored(){
            return colorRestored;
        }
    }
}

