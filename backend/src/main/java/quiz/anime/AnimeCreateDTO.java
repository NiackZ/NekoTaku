package quiz.anime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import quiz.links.Link;
import quiz.utils.model.Image;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimeCreateDTO {
    private Image poster;
    @NotNull
    private String rusName;
    private String romName;
    private Long typeId;
    private List<Long> genreIds;
    private List<Long> studioIds;
    @NotNull
    private Long statusId;
    @NotNull
    private List<LocalDate> period;
    @NotNull
    private Integer episodeCount;
    @NotNull
    private Integer episodeDuration;
    private List<Link> linkList;
    private List<Long> markIds;
    private String description;
}
