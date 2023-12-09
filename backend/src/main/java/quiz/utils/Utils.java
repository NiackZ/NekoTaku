package quiz.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Utils {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-";
    private static final int DEFAULT_LENGTH = 10;

    public static boolean listContainsValuesIgnoreCase(List<String> srcList, List<String> secondList) {
        return listContainsValues(srcList, secondList, true);
    }

    public static boolean listContainsValues(List<String> srcList, List<String> secondList, boolean ignoreCase) {
        return ignoreCase
               ? secondList.stream().allMatch(item -> srcList.stream().anyMatch(e -> e.equalsIgnoreCase(item)))
               : new HashSet<>(srcList).containsAll(secondList);
    }

    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static String generateRandomString(int length) {

        if (length <= 0) {
            throw new IllegalArgumentException("Число должно быть больше 0");
        }

        StringBuilder randomString = new StringBuilder(length);
        Random random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    public static String generateRandomString() {
        return generateRandomString(DEFAULT_LENGTH);
    }

    public static String convertToRelativePath(String absolutePath) {
        Path projectPath = Path.of(Paths.get("").toAbsolutePath() + "/frontend/");
        Path filePath = Paths.get(absolutePath);

        Path relativePath = projectPath.relativize(filePath);

        return "../" + relativePath.toString().replace("\\", "/");
    }
}
