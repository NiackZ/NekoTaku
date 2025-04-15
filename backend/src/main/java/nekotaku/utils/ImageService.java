package nekotaku.utils;

import nekotaku.utils.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    public static void updatePoster(Image poster, Long animeId, String oldPosterPath, String directory, Consumer<String> updatePathFn) throws IOException {
        try {
            String newPath = setPoster(poster, animeId, oldPosterPath, directory);
            updatePathFn.accept(newPath);
        } catch (IOException e) {
            logger.error("Ошибка при установке постера: {}", e.getMessage());
            throw e;
        }
    }

    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static String convertToRelativePath(String absolutePath) {
        Path projectPath = Path.of(Paths.get("").toAbsolutePath() + "/frontend/");
        Path filePath = Paths.get(absolutePath);

        Path relativePath = projectPath.relativize(filePath);

        return "/" + relativePath.toString().replace("\\", "/");
    }

    public static byte[] resizeImage(byte[] originalImage, String formatName, int targetWidth) throws IOException {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImage)) {
            BufferedImage originalBufferedImage = ImageIO.read(inputStream);
            if (originalBufferedImage == null) {
                throw new IllegalArgumentException("Не удалось обработать изображение");
            }
            int originalWidth = originalBufferedImage.getWidth();
            if (originalWidth <= targetWidth) {
                return originalImage;
            }
            int originalHeight = originalBufferedImage.getHeight();

            // Вычисляем новую высоту, сохраняя пропорции
            int targetHeight = (int) Math.round((double) targetWidth / originalWidth * originalHeight);

            // Создаем новое изображение с уменьшенными размерами
            java.awt.Image scaledImage = originalBufferedImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
            BufferedImage resizedBufferedImage = new BufferedImage(targetWidth, targetHeight, originalBufferedImage.getType());

            Graphics2D g2d = resizedBufferedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            // Конвертируем BufferedImage обратно в массив байтов
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedBufferedImage, formatName, outputStream);
            return outputStream.toByteArray();
        }
    }

    private static String setPoster(Image poster, Long id, String previousPosterUrl, String imgPath) throws IOException {
        try {
            if (poster != null) {
                Path absolutePath = Paths.get("").toAbsolutePath();
                String frontUrl = "/frontend/";
                //TODO переделать на deletePoster
                if (previousPosterUrl != null) {
                    Files.deleteIfExists(Paths.get(String.valueOf(absolutePath), frontUrl, previousPosterUrl));
                }
                String directoryPath = absolutePath + frontUrl + "src/public" + imgPath + id + "/";
                String formatName = getFileExtension(poster.getFileName());
                Files.createDirectories(Paths.get(directoryPath));

                byte[] fileBytes = java.util.Base64.getDecoder().decode(poster.getBase64Image());
                String fullPath = directoryPath + Utils.generateRandomString() + "." + formatName;
                Files.write(Paths.get(fullPath), resizeImage(fileBytes, formatName, 700));
                return convertToRelativePath(fullPath);
            }
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public static void deletePoster(String previousPosterUrl) throws IOException {
        try {
            if (previousPosterUrl != null) {
                Files.deleteIfExists(Paths.get(Paths.get("").toAbsolutePath() + "/frontend/" + previousPosterUrl));
            }
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
