package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.adapter.out.mysql.entity.AsientoEntity;
import com.stagepass.live.adapter.out.mysql.entity.EventoEntity;
import com.stagepass.live.adapter.out.mysql.entity.RecintoEntity;
import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;

/**
 * Traduce entre las entidades JPA (infraestructura) y los modelos de dominio.
 * Ningun objeto JPA cruza nunca hacia la capa de aplicacion o dominio.
 */
final class CatalogoMapper {

    private CatalogoMapper() {
    }

    static Recinto aDominio(RecintoEntity entity) {
        return new Recinto(entity.getId(), entity.getNombre(), entity.getAforo());
    }

    static Evento aDominio(EventoEntity entity) {
        return new Evento(entity.getId(), entity.getNombre(), entity.getTipo(),
                entity.getRecintoId(), entity.getFechaHora(), entity.getEstado());
    }

    static Asiento aDominio(AsientoEntity entity) {
        return new Asiento(entity.getId(), entity.getRecintoId(), entity.getCodigo(), entity.getPrecioBase());
    }
}
