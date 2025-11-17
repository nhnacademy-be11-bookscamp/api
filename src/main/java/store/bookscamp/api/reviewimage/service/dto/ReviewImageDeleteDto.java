package store.bookscamp.api.reviewimage.service.dto;

import java.util.List;

public record ReviewImageDeleteDto (
        List<String> imageUrls
) {}

