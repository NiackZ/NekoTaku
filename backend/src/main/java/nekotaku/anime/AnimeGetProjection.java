package nekotaku.anime;

import java.time.LocalDate;
import java.util.List;

public interface AnimeGetProjection {
    Long getId();
    String getRuName();
    String getRomajiName();
    String getPosterURL();
    String getTypeName(); // Возвращает имея типа
    List<String> getGenreListName(); // Возвращает список имен жанров
    LocalDate getStartDate();
}
