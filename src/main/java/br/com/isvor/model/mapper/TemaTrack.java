package br.com.isvor.model.mapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class TemaTrack {

    private String profissionalId;

    private String temaId;

    private String progresso;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataComentario;

    private Short versao;

    private Short ativo;

}
