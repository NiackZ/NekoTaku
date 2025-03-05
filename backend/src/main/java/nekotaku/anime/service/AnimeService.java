package nekotaku.anime.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import nekotaku.anime.*;
import nekotaku.anime.dto.AnimeCreateDTO;
import nekotaku.anime.dto.AnimeGetDTO;
import nekotaku.anime.dto.AnimeGetShortDTO;
import nekotaku.anime.repository.AnimeRepository;
import nekotaku.genres.GenreRepository;
import nekotaku.links.Link;
import nekotaku.links.LinkService;
import nekotaku.marks.MarkRepository;
import nekotaku.status.StatusRepository;
import nekotaku.studios.StudioRepository;
import nekotaku.types.TypeRepository;
import nekotaku.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;
    private final TypeRepository typeRepository;
    private final GenreRepository genreRepository;
    private final StatusRepository statusRepository;
    private final MarkRepository markRepository;
    private final StudioRepository studioRepository;
    private final LinkService linkService;

    private final Logger logger = LoggerFactory.getLogger(AnimeService.class);

    @Transactional
    public Long createAnime(AnimeCreateDTO animeCreateDTO) throws IOException {
        Anime anime = new Anime();
        anime.setRuName(animeCreateDTO.getRusName());
        anime.setRomajiName(animeCreateDTO.getRomName());
        anime.setPosterURL(null);
        anime.setEpisodeCount(animeCreateDTO.getEpisodeCount());
        anime.setEpisodeDuration(animeCreateDTO.getEpisodeDuration());
        anime.setDescription(animeCreateDTO.getDescription());
        List<LocalDate> period = animeCreateDTO.getPeriod();
        anime.setStartDate(period.isEmpty() ? null : period.get(0));
        anime.setEndDate(period.size() > 1 ? period.get(1) : null);

        anime.setType(typeRepository.findById(animeCreateDTO.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Тип не найден")));
        anime.setStatus(statusRepository.findById(animeCreateDTO.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Статус не найден")));

        anime.setGenres(genreRepository.findAllById(animeCreateDTO.getGenreIds()));
        anime.setStudios(studioRepository.findAllById(animeCreateDTO.getStudioIds()));
        anime.setMarks(markRepository.findAllById(animeCreateDTO.getMarkIds()));
        anime.setLinks(animeCreateDTO.getLinkList().stream()
                .map(linkService::createNewLink)
                .toList());

        Long animeId = animeRepository.save(anime).getId();
        animeRepository.updatePoster(
                Utils.setPoster(animeCreateDTO.getPoster(), animeId, null, "/images/poster/anime/"),
                animeId
        );
        return animeId;
    }

    public List<AnimeGetDTO> getAllAnimes() {
        return animeRepository.findAll().stream()
                .map(AnimeGetDTO::new)
                .toList();
    }

    public List<AnimeGetShortDTO> getAllAnimesShort() {
        return animeRepository.findAll().stream()
                .map(AnimeGetShortDTO::new)
                .toList();
    }

    public Anime getAnimeById(Long id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Аниме не найдено"));
    }

    @Transactional
    public void updateAnime(Long id, AnimeCreateDTO anime) throws IOException {
        Anime currentAnime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Аниме для обновления не найдено"));

        currentAnime.setRuName(anime.getRusName());
        currentAnime.setRomajiName(anime.getRomName());
        currentAnime.setEpisodeCount(anime.getEpisodeCount());
        currentAnime.setEpisodeDuration(anime.getEpisodeDuration());
        currentAnime.setDescription(anime.getDescription());

        List<LocalDate> period = anime.getPeriod();
        currentAnime.setStartDate(period.isEmpty() ? null : period.get(0));
        currentAnime.setEndDate(period.size() > 1 ? period.get(1) : null);

        updateType(currentAnime, anime.getTypeId());
        updateStatus(currentAnime, anime.getStatusId());
        updateCollection(currentAnime::setGenres, genreRepository, anime.getGenreIds(), "жанров");
        updateCollection(currentAnime::setStudios, studioRepository, anime.getStudioIds(), "студий");
        updateLinks(currentAnime, anime.getLinkList());

        animeRepository.save(currentAnime);

        animeRepository.updatePoster(
                Utils.setPoster(anime.getPoster(), currentAnime.getId(), currentAnime.getPosterURL(), "/images/poster/anime/"),
                currentAnime.getId()
        );
    }

    private void updateType(Anime anime, Long typeId) {
        if (typeId != null) {
            anime.setType(typeRepository.findById(typeId)
                    .orElseThrow(() -> new EntityNotFoundException("Тип с ИД = " + typeId + " не найден")));
        }
    }

    private void updateStatus(Anime anime, Long statusId) {
        if (statusId != null) {
            anime.setStatus(statusRepository.findById(statusId)
                    .orElseThrow(() -> new EntityNotFoundException("Статус с ИД = " + statusId + " не найден")));
        }
    }

    private <T> void updateCollection(Consumer<List<T>> setter, JpaRepository<T, Long> repository, List<Long> ids, String entityName) {
        if (ids != null && !ids.isEmpty()) {
            List<T> entities = repository.findAllById(ids);
            if (entities.size() != ids.size()) {
                throw new EntityNotFoundException("Количество " + entityName + " не совпадает");
            }
            setter.accept(entities);
        } else {
            setter.accept(Collections.emptyList());
        }
    }

    private void updateLinks(Anime anime, List<Link> newLinks) {
        List<Link> currentLinks = anime.getLinks();

        // Удаляем отсутствующие в новом списке ссылки
        List<Link> linksToRemove = currentLinks.stream()
                .filter(existingLink -> newLinks.stream().noneMatch(updatedLink ->
                        updatedLink.getId() != null && updatedLink.getId().equals(existingLink.getId())))
                .toList();
        anime.getLinks().removeAll(linksToRemove);

        // Обновляем существующие ссылки или добавляем новые
        for (Link updatedLink : newLinks) {
            if (updatedLink.getId() != null) {
                currentLinks.stream()
                        .filter(existingLink -> updatedLink.getId().equals(existingLink.getId()))
                        .findFirst()
                        .ifPresent(existingLink -> {
                            existingLink.setName(updatedLink.getName());
                            existingLink.setUrl(updatedLink.getUrl());
                        });
            } else {
                anime.getLinks().add(linkService.createNewLink(updatedLink));
            }
        }

        // Удаляем ссылки из базы данных, которые больше не используются
        linkService.deleteAll(linksToRemove);
    }

    public void deleteAnime(Long id) {
        // Реализация удаления с флагом isDeleted = 1
    }

    public List<AnimeGetShortDTO> searchAnime(String text) {
        Specification<Anime> spec = AnimeSpecification.search(clearTitle(text));
        return animeRepository.findAll(spec).stream()
                .map(AnimeGetShortDTO::new)
                .toList();
    }

    private String clearTitle(String title) {
        if (title == null) return null;
        return title.trim()
                .replaceAll("[^\\p{L}\\d\\s-]", "")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

}
