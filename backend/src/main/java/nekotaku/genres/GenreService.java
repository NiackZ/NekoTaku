package nekotaku.genres;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {

    private final Logger logger = LoggerFactory.getLogger(GenreService.class);
    public static final String CACHE_NAME = "genres";
    private final GenreRepository repository;
    private final String NOT_FOUND = "Жанр не найден. ID = ";
    private final String EXIST = "Жанр с таким именем уже существует.";

    @Cacheable(CACHE_NAME)
    public List<Genre> findAll() {
        return repository.findAll();
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Genre save(Genre genre) {
        try {
            if (repository.existsByName(genre.getName())) {
                throw new EntityExistsException(EXIST);
            }
            return repository.save(genre);
        } catch (EntityExistsException e) {
            logger.error("Жанр с именем '{}' уже существует. ", genre.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при сохранении жанра: {}", genre.getName(), e);
            throw new IllegalArgumentException("Не удалось создать новый жанр", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Genre update(Long id, Genre updatedGenre) {
        try {
            Genre genre = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));

            boolean isNameChanged = !genre.getName().equalsIgnoreCase(updatedGenre.getName());

            if (isNameChanged && repository.existsByName(updatedGenre.getName())) {
                throw new EntityExistsException(EXIST);
            }

            genre.setName(updatedGenre.getName());
            genre.setDeleted(updatedGenre.isDeleted());
            return repository.save(genre);
        }
        catch (EntityNotFoundException e) {
            logger.error("Жанр с ИД {} не найден.", updatedGenre.getId(), e);
            throw e;
        }
        catch (EntityExistsException e) {
            logger.error("Жанр с именем '{}' уже существует.", updatedGenre.getName(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при сохранении жанра: {}", updatedGenre.getName(), e);
            throw new IllegalArgumentException("Не удалось сохранить жанр", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteById(Long id) {
        try {
            Genre genre = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));
            genre.setDeleted(true);
            repository.save(genre);
        }
        catch (EntityNotFoundException e) {
            logger.error("Жанр с ИД {} не найден.", id, e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при удалении жанра с ИД {}", id, e);
            throw new IllegalArgumentException("Не удалось удалить жанр", e);
        }
    }

}
