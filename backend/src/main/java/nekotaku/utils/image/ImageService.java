package nekotaku.utils.image;

import nekotaku.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private static final Path projectRoot = Paths.get("").toAbsolutePath();
    private static final Path frontendDir = projectRoot.resolve("frontend");
    private static final Path imagesDir = frontendDir.resolve("src").resolve("images");

    public static String resizeAndSave(MultipartFile file,
                                           int targetWidth,
                                           int targetHeight,
                                           ScalingStrategy scalingStrategy,
                                           OutputFormat outputFormat) throws ImageProcessingException, IOException {
        return resizeAndSave(file, targetWidth, targetHeight, scalingStrategy, outputFormat, null);
    }
    public static String resizeAndSave(MultipartFile file,
                                       int targetWidth,
                                       int targetHeight,
                                       ScalingStrategy scalingStrategy,
                                       OutputFormat outputFormat,
                                       String filePath) throws IOException, ImageProcessingException {
        long startTime = System.currentTimeMillis();

        // Проверяем MIME-тип
        String mimeType = file.getContentType();
        if (!isAllowedFormat(mimeType)) {
            logger.error("Формат изображения не поддерживается: {}", mimeType);
            throw new ImageProcessingException("Формат изображения не поддерживается: " + mimeType);
        }

        BufferedImage srcImage;
        try (InputStream is = file.getInputStream()) {
            srcImage = ImageIO.read(is);
            if (srcImage == null) {
                throw new IOException("Не удалось распознать изображение.");
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении изображения.", e);
            throw new IOException("Ошибка при чтении изображения.", e);
        }

        int originalWidth = srcImage.getWidth();
        int originalHeight = srcImage.getHeight();
        BufferedImage processedImage;
        int newWidth;
        int newHeight;

        try {
            switch (scalingStrategy) {
                case FIT_TO_WIDTH:
                    newWidth = targetWidth;
                    newHeight = (int) ((double) targetWidth / originalWidth * originalHeight);
                    processedImage = getScaledImage(srcImage, newWidth, newHeight);
                    break;
                case FIT_TO_HEIGHT:
                    newHeight = targetHeight;
                    newWidth = (int) ((double) targetHeight / originalHeight * originalWidth);
                    processedImage = getScaledImage(srcImage, newWidth, newHeight);
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестная стратегия масштабирования");
            }
        } catch (Exception e) {
            logger.error("Ошибка при масштабировании изображения", e);
            throw new ImageProcessingException("Ошибка при масштабировании изображения.", e);
        }

        Path outputPath = prepareOutputPath(filePath, outputFormat);

        // Сохранение изображения
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            boolean writeResult = ImageIO.write(processedImage, outputFormat.getExtension(), os);
            if (!writeResult) {
                throw new IOException("Не удалось записать изображение в формате " + outputFormat);
            }
        } catch (IOException e) {
            logger.error("Ошибка при сохранении файла", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        logger.info("Обработка изображения завершена за {} мс, сохранено в {}", (endTime - startTime), outputPath.toAbsolutePath());
        return trimToSrcPath(outputPath.toAbsolutePath());
    }

    private static boolean isAllowedFormat(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String[] parts = mimeType.toLowerCase().split("/");
        if (parts.length != 2) {
            return false;
        }
        if (!"image".equals(parts[0])) {
            return false;
        }
        List<String> allowed = List.of("png", "jpg", "jpeg", "webp", "jfif");
        String subtype = parts[1];
        return allowed.contains(subtype);
    }

    private static BufferedImage getScaledImage(BufferedImage srcImage, int targetWidth, int targetHeight) {
        BufferedImage image = LanczosResizer.resizeImageLanczos(srcImage, targetWidth, targetHeight, 3);
        return ImageProcessingUtil.applyMildSmoothing(image);
    }
//
//    /**
//     * Вспомогательный метод для масштабирования изображения с использованием высококачественной
//     * интерполяции (BICUBIC), что обеспечивает более плавное сглаживание.
//     *
//     * @param srcImage исходное изображение
//     * @param targetWidth целевая ширина
//     * @param targetHeight целевая высота
//     * @return масштабированное изображение
//     */
//    private BufferedImage getScaledImageOld(BufferedImage srcImage, int targetWidth, int targetHeight) {
//        // Если исходное изображение не поддерживает альфа-канал, можно использовать TYPE_INT_RGB,
//        // либо TYPE_INT_ARGB, если требуется прозрачность
//        int imageType = srcImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : srcImage.getType();
//        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, imageType);
//        Graphics2D g2d = resizedImage.createGraphics();
//        // Устанавливаем высокое качество рендеринга
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
//        g2d.dispose();
//        return resizedImage;
//    }
//
//    private void applyQualityRenderingHints(Graphics2D g2d) {
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
//    }
//
//
//    public Path resizeWithHighQuality(BufferedImage originalImage,
//                                      int targetWidth,
//                                      int targetHeight,
//                                      ScalingStrategy scalingStrategy,
//                                      OutputFormat outputFormat,
//                                      Path outputDir,
//                                      float compressionQuality) throws IOException {
//
//        // 1. Вычисляем размеры с учётом стратегии
//        Dimension newSize = calculateNewSize(originalImage.getWidth(), originalImage.getHeight(),
//                targetWidth, targetHeight, scalingStrategy);
//
//        int imageType = (outputFormat == OutputFormat.PNG || hasAlpha(originalImage)) ?
//                BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
//
//        // 2. Создаём пустое изображение
//        BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, imageType);
//
//        // 3. Гладкое масштабирование
//        Graphics2D g2d = resizedImage.createGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//        g2d.drawImage(originalImage, 0, 0, newSize.width, newSize.height, null);
//        g2d.dispose();
//
//        // 4. Генерация имени файла
//        String extension = outputFormat.name().toLowerCase();
//        String uniqueName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "." + extension;
//        Files.createDirectories(outputDir);
//        Path outputFilePath = outputDir.resolve(uniqueName);
//        // 5. Сохраняем с нужным качеством
//        try (OutputStream os = Files.newOutputStream(outputFilePath)) {
//            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
//                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
//                ImageWriter writer = writers.next();
//
//                try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
//                    writer.setOutput(ios);
//
//                    ImageWriteParam param = writer.getDefaultWriteParam();
//                    if (param.canWriteCompressed()) {
//                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//                        param.setCompressionQuality(compressionQuality); // 0.0f - 1.0f
//                    }
//
//                    writer.write(null, new IIOImage(resizedImage, null, null), param);
//                } finally {
//                    writer.dispose();
//                }
//            } else {
//                ImageIO.write(resizedImage, extension, os); // PNG, WEBP (если настроен)
//            }
//        }
//
//        logger.info(String.valueOf(outputFilePath.toAbsolutePath()));
//        return outputFilePath;
//    }
//
//    private BufferedImage resizeWithHighQuality(BufferedImage originalImage, int width, int height) {
//        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = outputImage.createGraphics();
//
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        g2d.drawImage(originalImage, 0, 0, width, height, null);
//        g2d.dispose();
//
//        return outputImage;
//    }
//
//
//    private Dimension calculateNewSize(int originalWidth, int originalHeight,
//                                       int targetWidth, int targetHeight,
//                                       ScalingStrategy strategy) {
//
//        switch (strategy) {
//            case FIT_TO_WIDTH -> {
//                float ratio = (float) targetWidth / originalWidth;
//                return new Dimension(targetWidth, Math.round(originalHeight * ratio));
//            }
//            case FIT_TO_HEIGHT -> {
//                float ratio = (float) targetHeight / originalHeight;
//                return new Dimension(Math.round(originalWidth * ratio), targetHeight);
//            }
//            default -> {
//                return new Dimension(originalWidth, originalHeight); // без изменений
//            }
//        }
//    }
//
//    private boolean hasAlpha(BufferedImage image) {
//        return image.getColorModel().hasAlpha();
//    }

    public static void deleteImageByPath(Path path) throws IOException {
        logger.info("Удаление {}", path.toAbsolutePath());
        if (Files.exists(path)) {
            Files.deleteIfExists(path);
        }
    }

    public static Path prepareOutputPath(String filePath, OutputFormat outputFormat) throws IOException {
        // Убедимся, что папка существует
        Files.createDirectories(imagesDir);

        // Выбираем имя файла: либо заданный путь, либо генерируем своё
        String fileName = (filePath == null || filePath.isBlank())
                ? generateFileName()
                : projectRoot + filePath;

        // Собираем итоговый путь
        return imagesDir.resolve(fileName + "." + outputFormat.getExtension()).toAbsolutePath();
    }

    private static String generateFileName() {
        return Utils.generateRandomString() + "_" + System.currentTimeMillis();
    }

    private static String trimToSrcPath(Path absolutePath) {
        Iterator<Path> it = absolutePath.iterator();
        int srcIndex = 0;
        for (int i = 0; it.hasNext(); i++) {
            Path part = it.next();
            if ("src".equals(part.toString())) {
                srcIndex = i;
                break;
            }
        }
        Path relative = absolutePath.subpath(srcIndex, absolutePath.getNameCount());
        return File.separator + relative.toString();
    }

    /**
     * Строит и возвращает путь к файлу постера для удаления.
     *
     * @param posterUrl относительный URL, например "/src/public/images/.../file.jpg"
     */
    public static Path buildDeletePath(String posterUrl) {
        Path raw = Paths.get(frontendDir.toString(), posterUrl);
        Path normalized = raw.normalize();
        if (!normalized.startsWith(frontendDir)) {
            throw new SecurityException("Попытка удаления файла вне разрешённой директории");
        }
        return normalized;
    }

}
