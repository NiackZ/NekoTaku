package nekotaku.studios;

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
public class StudioService {

    private final Logger logger = LoggerFactory.getLogger(StudioService.class);
    public  static final String CACHE_NAME = "studios";
    private final StudioRepository repository;
    private final String NOT_FOUND = "Студия не найден. ID = ";
    private final String EXIST = "Студия с таким именем уже существует.";

    @Cacheable(value = CACHE_NAME)
    public List<Studio> findAll() {
        return repository.findAll();
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Studio save(Studio studio) {
        try {
            if (repository.existsByName(studio.getName())) {
                throw new EntityExistsException(EXIST);
            }
            return repository.save(studio);
        } catch (EntityExistsException e) {
            logger.error("Студия с именем '{}' уже существует. ", studio.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при сохранении студии: {}", studio.getName(), e);
            throw new IllegalArgumentException("Не удалось создать новую студию", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Studio update(Long id, Studio updatedStudio) {
        try {
            Studio studio = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));

            boolean isNameChanged = !studio.getName().equalsIgnoreCase(updatedStudio.getName());

            if (isNameChanged && repository.existsByName(updatedStudio.getName())) {
                throw new EntityExistsException(EXIST);
            }

            studio.setName(updatedStudio.getName());
            studio.setDeleted(updatedStudio.isDeleted());
            return repository.save(studio);
        }
        catch (EntityNotFoundException e) {
            logger.error("Студия с ИД {} не найден.", updatedStudio.getId(), e);
            throw e;
        }
        catch (EntityExistsException e) {
            logger.error("Студия с именем '{}' уже существует.", updatedStudio.getName(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при сохранении студии: {}", updatedStudio.getName(), e);
            throw new IllegalArgumentException("Не удалось сохранить студию", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteById(Long id) {
        try {
            Studio studio = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));
            studio.setDeleted(true);
            repository.save(studio);
        }
        catch (EntityNotFoundException e) {
            logger.error("Студия с ИД {} не найден.", id, e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при удалении студии с ИД {}", id, e);
            throw new IllegalArgumentException("Не удалось удалить студию", e);
        }
    }
}
