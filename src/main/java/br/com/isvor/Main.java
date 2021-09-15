package br.com.isvor;

import br.com.isvor.service.TemaTrackConverterService;

public class Main {

    private final static TemaTrackConverterService service = new TemaTrackConverterService();

    public static void main(String[] args) {
        service.execute();
    }
}
