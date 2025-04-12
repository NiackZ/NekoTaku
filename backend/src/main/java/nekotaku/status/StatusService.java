package nekotaku.status;

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
public class StatusService {

    private final Logger logger = LoggerFactory.getLogger(StatusService.class);
    public static final String CACHE_NAME = "statuses";
    private final StatusRepository repository;
    private final String NOT_FOUND = "Статус не найден. ID = ";
    private final String EXIST = "Статус с таким именем уже существует.";

    @Cacheable(CACHE_NAME)
    public List<Status> findAll() {
        return repository.findAll();
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Status save(Status status) {
        try {
            if (repository.existsByName(status.getName())) {
                throw new EntityExistsException(EXIST);
            }
            return repository.save(status);
        } catch (EntityExistsException e) {
            logger.error("Статус с именем '{}' уже существует. ", status.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при сохранении статуса: {}", status.getName(), e);
            throw new IllegalArgumentException("Не удалось создать новый статус", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Status update(Long id, Status updatedStatus) {
        try {
            Status status = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));

            boolean isNameChanged = !status.getName().equalsIgnoreCase(updatedStatus.getName());

            if (isNameChanged && repository.existsByName(updatedStatus.getName())) {
                throw new EntityExistsException(EXIST);
            }

            status.setName(updatedStatus.getName());
            status.setDeleted(updatedStatus.isDeleted());
            return repository.save(status);
        }
        catch (EntityNotFoundException e) {
            logger.error("Статус с ИД {} не найден.", updatedStatus.getId(), e);
            throw e;
        }
        catch (EntityExistsException e) {
            logger.error("Статус с именем '{}' уже существует.", updatedStatus.getName(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при сохранении статуса: {}", updatedStatus.getName(), e);
            throw new IllegalArgumentException("Не удалось сохранить статус", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteById(Long id) {
        try {
            Status status = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));
            status.setDeleted(true);
            repository.save(status);
        }
        catch (EntityNotFoundException e) {
            logger.error("Статус с ИД {} не найден.", id, e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при удалении статуса с ИД {}", id, e);
            throw new IllegalArgumentException("Не удалось удалить статус", e);
        }
    }

}
