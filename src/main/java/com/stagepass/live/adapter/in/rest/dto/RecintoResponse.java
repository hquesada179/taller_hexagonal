package com.stagepass.live.adapter.in.rest.dto;

import com.stagepass.live.domain.model.Recinto;

public record RecintoResponse(String id, String nombre, int aforo) {

    public static RecintoResponse desde(Recinto recinto) {
        return new RecintoResponse(recinto.getId(), recinto.getNombre(), recinto.getAforo());
    }
}
