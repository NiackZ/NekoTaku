package nekotaku.anime.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nekotaku.anime.Anime;
import nekotaku.anime.dto.AnimeCreateDTO;
import nekotaku.anime.dto.AnimeGetDTO;
import nekotaku.anime.dto.AnimeGetShortDTO;
import nekotaku.anime.service.AnimeService;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/anime")
@AllArgsConstructor
public class AnimeController {
    private final AnimeService animeService;

      @GetMapping
    public ResponseEntity<List<AnimeGetDTO>> getAll() {
        return ResponseEntity.ok(animeService.getAllAnimes());
    }

    @GetMapping("/short")
    public ResponseEntity<List<AnimeGetShortDTO>> getAllShortInfo() {
        return ResponseEntity.ok(animeService.getAllAnimesShort());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.getAnimeById(id));
    }

    @PostMapping
    public ResponseEntity<Long> createAnime(@RequestBody @NotNull AnimeCreateDTO anime) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.createAnime(anime));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAnime(@PathVariable Long id, @RequestBody AnimeCreateDTO anime) throws IOException {
        animeService.updateAnime(id, anime);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<AnimeGetShortDTO>> search(@RequestBody @NotNull String text)
            throws InterruptedException {
        return ResponseEntity.ok(animeService.searchAnime(text));
    }

}
