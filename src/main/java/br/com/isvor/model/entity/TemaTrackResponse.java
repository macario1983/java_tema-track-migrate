package br.com.isvor.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TemaTrackResponse {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("profissionalId")
    private String profissionalId;

    @JsonProperty("temaId")
    private String temaId;

    @JsonProperty("temaTracking")
    private String temaTracking;

}
