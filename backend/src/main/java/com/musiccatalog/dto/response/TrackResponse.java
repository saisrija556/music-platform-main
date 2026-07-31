package com.musiccatalog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackResponse {

    private Long trackId;
    private String trackName;
    private String previewUrl;
    private Integer trackNumber;
}
