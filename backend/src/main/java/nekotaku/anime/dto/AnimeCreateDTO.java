package nekotaku.anime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nekotaku.links.Link;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimeCreateDTO {
    @NotNull
    private String rusName;
    private String romName;
    private Long typeId;
    private List<Long> genreIds;
    private List<Long> studioIds;
    @NotNull
    private Long statusId;
    private List<LocalDate> period;
    private Integer episodeCount;
    private Integer episodeDuration;
    private List<Link> linkList;
    private List<Long> markIds;
    private String description;
}
