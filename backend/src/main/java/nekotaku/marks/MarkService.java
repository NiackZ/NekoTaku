package nekotaku.marks;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MarkService {

    private final MarkRepository markRepository;
    private final String ALL_ENTITY_KEY = "'all_marks'";
    private final String CACHE_NAME = "marks";

    @Cacheable(value = CACHE_NAME, key = ALL_ENTITY_KEY)
    public List<Mark> findAll() {
        return markRepository.findAll();
    }
}
