package com.musiccatalog.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLibraryItemRequest {

    @Min(1)
    @Max(5)
    private Short userRating;

    @Size(max = 5000)
    private String userNotes;
}
