package nekotaku.types;

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
public class TypeService {

    private final Logger logger = LoggerFactory.getLogger(TypeService.class);
    public static final String CACHE_NAME = "types";
    private final TypeRepository repository;
    private final String NOT_FOUND = "Тип не найден. ID = ";
    private final String EXIST = "Тип с таким именем уже существует.";

    @Cacheable(CACHE_NAME)
    public List<Type> findAll() {
        try {
            return repository.findAll();
        }
        catch (Exception e) {
            throw new RuntimeException("Не удалось получить список типов", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Type save(Type type) {
        try {
            if (repository.existsByName(type.getName())) {
                throw new EntityExistsException(EXIST);
            }
            return repository.save(type);
        } catch (EntityExistsException e) {
            logger.error("Тип с именем '{}' уже существует. ", type.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при сохранении типа: {}", type.getName(), e);
            throw new IllegalArgumentException("Не удалось создать новый тип", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Type update(Long id, Type updatedType) {
        try {
            Type existingType = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));

            boolean isNameChanged = !existingType.getName().equalsIgnoreCase(updatedType.getName());

            if (isNameChanged && repository.existsByName(updatedType.getName())) {
                throw new EntityExistsException(EXIST);
            }

            existingType.setName(updatedType.getName());
            existingType.setDeleted(updatedType.isDeleted());
            return repository.save(existingType);
        }
        catch (EntityNotFoundException e) {
            logger.error("Тип с ИД {} не найден.", updatedType.getId(), e);
            throw e;
        }
        catch (EntityExistsException e) {
            logger.error("Тип с именем '{}' уже существует.", updatedType.getName(), e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при сохранении типа: {}", updatedType.getName(), e);
            throw new IllegalArgumentException("Не удалось сохранить тип", e);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteById(Long id) {
        try {
            Type type = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(NOT_FOUND + id));
            type.setDeleted(true);
            repository.save(type);
        }
        catch (EntityNotFoundException e) {
            logger.error("Тип с ИД {} не найден.", id, e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Произошла ошибка при удалении типа с ИД {}", id, e);
            throw new IllegalArgumentException("Не удалось удалить тип", e);
        }
    }

}
