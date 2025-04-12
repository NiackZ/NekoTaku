package nekotaku.anime.api;

import lombok.AllArgsConstructor;
import nekotaku.anime.dto.AnimeResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import nekotaku.anime.Anime;
import nekotaku.anime.dto.AnimeCreateDTO;
import nekotaku.anime.AnimeGetProjection;
import nekotaku.anime.AnimeGetShortProjection;
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
    public ResponseEntity<List<AnimeGetProjection>> getAll() {
        return ResponseEntity.ok(animeService.getAllAnimes());
    }

    @GetMapping("/short")
    public ResponseEntity<List<AnimeGetShortProjection>> getAllShortInfo() {
        return ResponseEntity.ok(animeService.getAllAnimesShort());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.getAnimeById(id));
    }

    @PostMapping
    public ResponseEntity<AnimeResponseDTO> createAnime(@RequestBody @NotNull AnimeCreateDTO anime) throws IOException {
        AnimeResponseDTO responseDTO = animeService.createAnime(anime);
        if (responseDTO.getMessage() != null) {
            return ResponseEntity.badRequest().body(responseDTO);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }


    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponseDTO> updateAnime(@PathVariable Long id, @RequestBody AnimeCreateDTO anime) throws IOException, InterruptedException {
        AnimeResponseDTO responseDTO = animeService.updateAnime(id, anime);
        Thread.sleep(2000);
        if (responseDTO.getMessage() != null) {
            return ResponseEntity.badRequest().body(responseDTO);
        }
        return ResponseEntity.ok().body(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<AnimeGetShortProjection>> search(@RequestBody @NotNull String text) {
        return ResponseEntity.ok(animeService.searchAnime(text));
    }

}
