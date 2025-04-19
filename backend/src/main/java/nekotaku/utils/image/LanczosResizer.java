package nekotaku.utils.image;

import java.awt.image.BufferedImage;

public class LanczosResizer {

    /**
     * Ресайз изображения с использованием свёртки по фильтру Ланцоша.
     *
     * @param src         Исходное изображение.
     * @param targetWidth Желаемая ширина.
     * @param targetHeight Желаемая высота.
     * @param a           Параметр окна Ланцоша (обычно 3).
     * @return Отмасштабированное изображение.
     */
    public static BufferedImage resizeImageLanczos(BufferedImage src, int targetWidth, int targetHeight, int a) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double scaleX = (double) srcWidth / targetWidth;
        double scaleY = (double) srcHeight / targetHeight;

        // Первый проход: горизонтальное ресайзирование
        BufferedImage intermediate = new BufferedImage(targetWidth, srcHeight, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < srcHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                double srcX = x * scaleX;
                int left = (int) Math.floor(srcX - a + 1);
                int right = (int) Math.ceil(srcX + a);
                double sumR = 0, sumG = 0, sumB = 0, sumA = 0, totalWeight = 0;
                for (int i = left; i <= right; i++) {
                    double weight = lanczos(srcX - i, a);
                    int mirroredX = mirrorClamp(i, 0, srcWidth - 1);
                    int pixel = src.getRGB(mirroredX, y);
                    int aVal = (pixel >> 24) & 0xff;
                    int rVal = (pixel >> 16) & 0xff;
                    int gVal = (pixel >> 8) & 0xff;
                    int bVal = pixel & 0xff;
                    sumA += aVal * weight;
                    sumR += rVal * weight;
                    sumG += gVal * weight;
                    sumB += bVal * weight;
                    totalWeight += weight;
                }
                int aOut = totalWeight != 0 ? clamp((int)Math.round(sumA / totalWeight), 0, 255) : 0;
                int rOut = totalWeight != 0 ? clamp((int)Math.round(sumR / totalWeight), 0, 255) : 0;
                int gOut = totalWeight != 0 ? clamp((int)Math.round(sumG / totalWeight), 0, 255) : 0;
                int bOut = totalWeight != 0 ? clamp((int)Math.round(sumB / totalWeight), 0, 255) : 0;
                int outPixel = (aOut << 24) | (rOut << 16) | (gOut << 8) | bOut;
                intermediate.setRGB(x, y, outPixel);
            }
        }

        // Второй проход: вертикальное ресайзирование
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < targetWidth; x++) {
            for (int y = 0; y < targetHeight; y++) {
                double srcY = y * scaleY;
                int top = (int) Math.floor(srcY - a + 1);
                int bottom = (int) Math.ceil(srcY + a);
                double sumR = 0, sumG = 0, sumB = 0, sumA = 0, totalWeight = 0;
                for (int j = top; j <= bottom; j++) {
                    double weight = lanczos(srcY - j, a);
                    int mirroredY = mirrorClamp(j, 0, srcHeight - 1);
                    int pixel = intermediate.getRGB(x, mirroredY);
                    int aVal = (pixel >> 24) & 0xff;
                    int rVal = (pixel >> 16) & 0xff;
                    int gVal = (pixel >> 8) & 0xff;
                    int bVal = pixel & 0xff;
                    sumA += aVal * weight;
                    sumR += rVal * weight;
                    sumG += gVal * weight;
                    sumB += bVal * weight;
                    totalWeight += weight;
                }
                int aOut = totalWeight != 0 ? clamp((int)Math.round(sumA / totalWeight), 0, 255) : 0;
                int rOut = totalWeight != 0 ? clamp((int)Math.round(sumR / totalWeight), 0, 255) : 0;
                int gOut = totalWeight != 0 ? clamp((int)Math.round(sumG / totalWeight), 0, 255) : 0;
                int bOut = totalWeight != 0 ? clamp((int)Math.round(sumB / totalWeight), 0, 255) : 0;
                int outPixel = (aOut << 24) | (rOut << 16) | (gOut << 8) | bOut;
                output.setRGB(x, y, outPixel);
            }
        }

        return output;
    }

    private static double lanczos(double x, int a) {
        if (x == 0.0) return 1.0;
        if (Math.abs(x) >= a) return 0.0;
        double piX = Math.PI * x;
        return (Math.sin(piX) / piX) * (Math.sin(piX / a) / (piX / a));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int mirrorClamp(int x, int min, int max) {
        if (x < min) return min + (min - x);
        if (x > max) return max - (x - max);
        return x;
    }
}