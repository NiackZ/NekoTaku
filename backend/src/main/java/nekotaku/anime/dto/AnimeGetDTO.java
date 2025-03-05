package nekotaku.anime.dto;

import lombok.Getter;
import lombok.Setter;
import nekotaku.anime.Anime;
import nekotaku.genres.Genre;
import nekotaku.types.Type;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AnimeGetDTO {
    private Long id;
    private String rusName;
    private String romName;
    private String posterURL;
    private Type type;
    private List<Genre> genreList;
    private LocalDate startDate;

    public AnimeGetDTO (Anime anime) {
        this.id = anime.getId();
        this.rusName = anime.getRuName();
        this.romName = anime.getRomajiName();
        this.posterURL = anime.getPosterURL();
        this.type = anime.getType();
        this.genreList = anime.getGenres();
        this.startDate = anime.getStartDate();
    }
}
