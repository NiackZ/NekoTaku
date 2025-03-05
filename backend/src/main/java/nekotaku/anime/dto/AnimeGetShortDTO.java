package nekotaku.anime.dto;

import lombok.Getter;
import lombok.Setter;
import nekotaku.anime.Anime;

@Setter
@Getter
public class AnimeGetShortDTO {
    private Long id;
    private String posterURL;
    private String ruName;

    public AnimeGetShortDTO (Anime anime) {
        this.id = anime.getId();
        this.posterURL = anime.getPosterURL();
        this.ruName = anime.getRuName();
    }
}
