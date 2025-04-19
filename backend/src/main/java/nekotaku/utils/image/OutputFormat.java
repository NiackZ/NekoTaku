package nekotaku.utils.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutputFormat {
    JPEG("jpg"),
    PNG("png"),
    WEBP("webp");

    private final String extension;

}
