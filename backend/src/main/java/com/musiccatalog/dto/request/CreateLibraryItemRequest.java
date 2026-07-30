package com.musiccatalog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLibraryItemRequest {

    @NotNull
    private Long appleCatalogId;

    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String artistName;

    @Size(max = 100)
    private String genre;

    private String releaseDate;

    private Integer trackCount;

    private String artworkUrl;
}
