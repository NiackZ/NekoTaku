package nekotaku.utils.image;

public class ImageProcessingException extends Exception {
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImageProcessingException(String message) {
        super(message);
    }
}
