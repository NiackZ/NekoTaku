package nekotaku.utils.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageProcessingUtil {

    /**
     * Применяет мягкое сглаживание изображения с использованием свёртки 3x3.
     * Это более мягкий фильтр, чем обычное медианное, который снижает мелкие шумы,
     * не вызывая чрезмерного размытия.
     *
     * @param src Исходное изображение.
     * @return Изображение после мягкого шумоподавления.
     */
    public static BufferedImage applyMildSmoothing(BufferedImage src) {
        // Ядро мягкого фильтра: центр больше, чем соседи
        float[] kernelData = {
                0.03125f, 0.03125f, 0.03125f,
                0.03125f, 0.75f,     0.03125f,
                0.03125f, 0.03125f, 0.03125f
        };

        Kernel kernel = new Kernel(3, 3, kernelData);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        op.filter(src, dest);
        return dest;
    }

    /**
     * Применяет гауссов фильтр 5x5 к исходному изображению.
     *
     * @param src Исходное изображение.
     * @return Изображение после применения Гауссова фильтра.
     */
    public static BufferedImage applyGaussianFilter(BufferedImage src) {
        // Классическое гауссово ядро 5x5, sigma примерно 1.0
        float[] kernelData = {
                1,  4,  6,  4,  1,
                4, 16, 24, 16,  4,
                6, 24, 36, 24,  6,
                4, 16, 24, 16,  4,
                1,  4,  6,  4,  1
        };

        // Нормируем ядро (сумма элементов должна быть равна 256)
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] /= 256f;
        }

        Kernel kernel = new Kernel(5, 5, kernelData);
        // Используем EDGE_NO_OP, чтобы края не менялись
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        op.filter(src, dest);
        return dest;
    }

    /**
     * Пример метода двойного ресайза с предварительным применением гауссова фильтра.
     *
     * @param original Исходное изображение.
     * @param targetWidth Желаемая ширина.
     * @param targetHeight Желаемая высота.
     * @return Отмасштабированное изображение.
     */
    public static BufferedImage resizeImageWithPreGaussian(BufferedImage original, int targetWidth, int targetHeight) {
        // Сначала применяем гауссов фильтр для сглаживания высоких частот
        BufferedImage smoothed = applyGaussianFilter(original);

        // Затем выполняем двойной ресайз
        BufferedImage resized = resizeImageDoublePass(smoothed, targetWidth, targetHeight);
        return resized;
    }

    /**
     * Метод двойного ресайза: сначала промежуточное уменьшение, затем финальный ресайз.
     *
     * @param src Исходное изображение.
     * @param targetWidth Желаемая ширина.
     * @param targetHeight Желаемая высота.
     * @return Отмасштабированное изображение.
     */
    public static BufferedImage resizeImageDoublePass(BufferedImage src, int targetWidth, int targetHeight) {
        int currentWidth = src.getWidth();
        int currentHeight = src.getHeight();

        BufferedImage img = src;

        // Постепенно уменьшаем изображение до тех пор, пока можно делить размеры на 2
        while (currentWidth / 2 >= targetWidth && currentHeight / 2 >= targetHeight) {
            currentWidth /= 2;
            currentHeight /= 2;

            BufferedImage tmp = new BufferedImage(currentWidth, currentHeight, src.getType());
            Graphics2D g2d = tmp.createGraphics();
            applyQualityRenderingHints(g2d);
            g2d.drawImage(img, 0, 0, currentWidth, currentHeight, null);
            g2d.dispose();

            img = tmp;
        }

        // Финальный ресайз до нужного размера
        BufferedImage finalImage = new BufferedImage(targetWidth, targetHeight, src.getType());
        Graphics2D g2d = finalImage.createGraphics();
        applyQualityRenderingHints(g2d);
        g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return finalImage;
    }

    /**
     * Устанавливает набор hints для качественного рендеринга.
     *
     * @param g2d Объект Graphics2D для установки настроек.
     */
    public static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING,
                java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING,
                java.awt.RenderingHints.VALUE_DITHER_ENABLE);
    }
}