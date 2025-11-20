package store.bookscamp.api.rank.controller.request;

import store.bookscamp.api.rank.service.dto.RankGetDto;

public record RankGetRequest(

        String name,
        int value
) {
    public static RankGetRequest fromDto(RankGetDto dto){
        return new RankGetRequest(
                dto.name(),
                dto.value()
        );
    }
}
