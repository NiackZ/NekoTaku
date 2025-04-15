package nekotaku.anime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nekotaku.genres.Genre;
import nekotaku.links.Link;
import nekotaku.marks.Mark;
import nekotaku.studios.Studio;
import nekotaku.title.model.Title;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "animes")
public class Anime extends Title {
    private String romajiName;
    private String kanjiName;
    @ManyToMany
    @JoinTable(
            name = "anime_studios",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id")
    )
    private List<Studio> studios;
    @ManyToMany
    @JoinTable(
            name = "anime_genres",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;
    @OneToMany
    @JoinTable(
            name = "anime_links",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id")
    )
    private List<Link> links = new ArrayList<>();
    @OneToMany
    @JoinTable(
            name = "anime_marks",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "mark_id")
    )
    private List<Mark> marks;
}
