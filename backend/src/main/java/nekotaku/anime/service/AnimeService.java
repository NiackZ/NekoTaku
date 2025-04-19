package nekotaku.anime.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import nekotaku.anime.Anime;
import nekotaku.anime.AnimeGetProjection;
import nekotaku.anime.AnimeGetShortProjection;
import nekotaku.anime.dto.AnimeCreateDTO;
import nekotaku.anime.repository.AnimeRepository;
import nekotaku.genres.GenreRepository;
import nekotaku.links.LinkService;
import nekotaku.marks.MarkRepository;
import nekotaku.status.StatusRepository;
import nekotaku.studios.StudioRepository;
import nekotaku.types.TypeRepository;
import nekotaku.utils.Utils;
import nekotaku.utils.image.ImageProcessingException;
import nekotaku.utils.image.ImageService;
import nekotaku.utils.image.OutputFormat;
import nekotaku.utils.image.ScalingStrategy;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public Long createAnime(AnimeCreateDTO dto, MultipartFile poster) {
        try {
            Anime anime = new Anime();
            fillAnimeFromDto(anime, dto);
            Long id = animeRepository.save(anime).getId();

            setAnimePoster(id, poster);
            return id;
        } catch (IOException | ImageProcessingException e) {
            logger.error("Ошибка при создании Аниме", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Long updateAnime(Long id, AnimeCreateDTO dto, MultipartFile poster) throws IOException, ImageProcessingException {
        try {
            Anime anime = animeRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(String.format("Аниме '%s' не найдено", id)));
            fillAnimeFromDto(anime, dto);
            animeRepository.save(anime);
            if (poster != null) {
                setAnimePoster(id, poster);
                ImageService.deleteImageByPath(ImageService.buildDeletePath(anime.getPosterURL()));
            }
        }
        catch (EntityNotFoundException e) {
            logger.error(e.getMessage());
            throw new EntityNotFoundException(String.format("Аниме '%s' не найдено", id), e);
        }
        catch (ImageProcessingException e) {
            logger.error(e.getMessage());
            throw new ImageProcessingException("Ошибка при обработке изображения", e);
        }
        return id;
    }

    private void setAnimePoster(Long animeId, MultipartFile poster) throws IOException, ImageProcessingException {
        String posterPath = ImageService.resizeAndSave(
                poster, 700, 1000, ScalingStrategy.FIT_TO_WIDTH, OutputFormat.JPEG,
                "/frontend/src/public/images/poster/anime/" + animeId + "/poster_" + Utils.generateRandomString()
        );
        animeRepository.updatePoster(posterPath, animeId);
    }

    private void fillAnimeFromDto(Anime anime, AnimeCreateDTO dto) {
        anime.setRuName(dto.getRusName());
        anime.setRomajiName(dto.getRomName());
        anime.setEpisodeCount(dto.getEpisodeCount());
        anime.setEpisodeDuration(dto.getEpisodeDuration());
        anime.setDescription(dto.getDescription());

        List<LocalDate> period = ListUtils.defaultIfNull(dto.getPeriod(), Collections.emptyList());
        anime.setStartDate(period.isEmpty() ? null : period.get(0));
        anime.setEndDate(period.size() > 1 ? period.get(1) : null);

        updateType(anime, dto.getTypeId());
        updateStatus(anime, dto.getStatusId());
        updateCollection(anime::setGenres, genreRepository, dto.getGenreIds(), "жанров");
        updateCollection(anime::setStudios, studioRepository, dto.getStudioIds(), "студий");
        updateCollection(anime::setMarks, markRepository, dto.getMarkIds(), "меток");

        linkService.updateAnimeLinks(anime, dto.getLinkList());
    }

    //todo подумать над исключениями
    public List<AnimeGetProjection> getAllAnimes() {
        try {
            return animeRepository.findAllAnimesWithGenres();
        } catch (DataAccessException e) {
            logger.error("Произошла ошибка базы данных при получении аниме: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в getAllAnimes: {}", e.getMessage(), e);
            throw new ServiceException("Не удалось получить список аниме", e);
        }
    }

    public List<AnimeGetShortProjection> getAllAnimesShort() {
        try {
            return animeRepository.findAllMainPageInfo();
        } catch (DataAccessException e) {
            logger.error("Произошла ошибка базы данных при получении аниме: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в getAllAnimesShort: {}", e.getMessage(), e);
            throw new ServiceException("Не удалось получить список аниме", e);
        }
    }

    public Anime getAnimeById(Long id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Аниме не найдено"));
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
        if (ids == null || ids.isEmpty()) {
            setter.accept(Collections.emptyList());
            return;
        }

        List<T> entities = repository.findAllById(ids);
        if (entities.size() != ids.size()) {
            throw new EntityNotFoundException("Некоторые " + entityName + " не найдены");
        }

        setter.accept(entities);
    }


    public void deleteAnime(Long id) {
//        Anime anime = animeRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Аниме не найдено"));
//        anime.setDeleted(true);
//        animeRepository.save(anime);
    }

    public List<AnimeGetShortProjection> searchAnime(String text) {
        text = Utils.clearTitle(text);
        return animeRepository.findByRuNameIgnoreCaseContainingOrEnNameIgnoreCaseContainingOrRomajiNameIgnoreCaseContainingOrKanjiNameIgnoreCaseContaining(
                text, text, text, text
        );
    }


}
