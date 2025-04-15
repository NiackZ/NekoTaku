package nekotaku.links;

import lombok.AllArgsConstructor;
import nekotaku.anime.Anime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    public List<Link> findAll() {
        return linkRepository.findAll();
    }

    public Link createNewLink(Link link) {
        return this.linkRepository.save(link);
    }

    public void deleteAll(List<Link> linksToRemove) {
        linkRepository.deleteAll(linksToRemove);
    }

    public void updateAnimeLinks(Anime anime, List<Link> newLinks) {

        List<Link> currentLinks = anime.getLinks();

        // Удаляем отсутствующие в новом списке ссылки
        List<Link> toRemove = currentLinks.stream()
                .filter(existing -> newLinks.stream()
                        .noneMatch(updatedLink -> updatedLink.getId() != null && updatedLink.getId().equals(existing.getId())))
                .toList();

        anime.getLinks().removeAll(toRemove);

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
                anime.getLinks().add(createNewLink(updatedLink));
            }
        }

        deleteAll(toRemove);
    }

}
