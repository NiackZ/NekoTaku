package nekotaku.anime.api;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import nekotaku.anime.Anime;
import nekotaku.anime.AnimeGetProjection;
import nekotaku.anime.AnimeGetShortProjection;
import nekotaku.anime.dto.AnimeCreateDTO;
import nekotaku.anime.service.AnimeService;
import nekotaku.utils.image.ImageProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/anime")
@AllArgsConstructor
public class AnimeController {
    private final AnimeService animeService;

    @GetMapping
    public ResponseEntity<List<AnimeGetProjection>> getAll() {
        return ResponseEntity.ok(animeService.getAllAnimes());
    }

    @GetMapping("/compact")
    public ResponseEntity<List<AnimeGetShortProjection>> getAllShortInfo() {
        return ResponseEntity.ok(animeService.getAllAnimesShort());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.getAnimeById(id));
    }

    @PostMapping
    public ResponseEntity<Long> createAnime(@RequestPart("animeJson") AnimeCreateDTO anime,
                                            @RequestPart(value = "poster", required = false) MultipartFile poster)  {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.createAnime(anime, poster));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateAnime(@PathVariable Long id,
                                            @RequestPart("animeJson") AnimeCreateDTO anime,
                                            @RequestPart(value = "poster", required = false) MultipartFile poster)
            throws IOException, ImageProcessingException {
        return ResponseEntity.ok().body(animeService.updateAnime(id, anime, poster));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<AnimeGetShortProjection>> search(@RequestParam @NotNull String text) {
        return ResponseEntity.ok(animeService.searchAnime(text));
    }

}
