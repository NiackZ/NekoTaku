package nekotaku.anime.repository;

import nekotaku.anime.Anime;
import nekotaku.anime.AnimeGetProjection;
import nekotaku.anime.AnimeGetShortProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Long>, JpaSpecificationExecutor<Anime> {
    @Transactional
    @Modifying
    @Query("update Anime a set a.posterURL = ?1 where a.id = ?2")
    void updatePoster(String posterURL, Long id);

    @Query("select a.id as id, a.posterURL as posterURL, a.ruName as ruName from Anime a")
    List<AnimeGetShortProjection> findAllMainPageInfo();

    List<AnimeGetShortProjection> findByRuNameIgnoreCaseContainingOrEnNameIgnoreCaseContainingOrRomajiNameIgnoreCaseContainingOrKanjiNameIgnoreCaseContaining(
            String ruName, String enName, String romajiName, String kanjiName
    );

    @Query(value = "SELECT a.id, a.ru_name, a.romaji_name, a.posterurl, a.start_date, t.name AS typeName, " +
            "COALESCE(ARRAY_AGG(DISTINCT g.name), '{}') AS genreListName " +
            "FROM animes a " +
            "JOIN types t ON a.type_id = t.id " +
            "LEFT JOIN anime_genres ag ON a.id = ag.anime_id " +
            "LEFT JOIN genres g ON ag.genre_id = g.id " +
            "GROUP BY a.id, t.name", nativeQuery = true)
    List<AnimeGetProjection> findAllAnimesWithGenres();


}
