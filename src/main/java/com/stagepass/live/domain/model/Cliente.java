package com.stagepass.live.domain.model;

import java.util.Objects;

public class Cliente {

    private final String id;
    private final String nombreCompleto;
    private final String correo;
    private final String telefonoMovil;
    private final String nacionalidad;

    public Cliente(String id, String nombreCompleto, String correo, String telefonoMovil, String nacionalidad) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto, "nombreCompleto es obligatorio");
        this.correo = Objects.requireNonNull(correo, "correo es obligatorio");
        this.telefonoMovil = Objects.requireNonNull(telefonoMovil, "telefonoMovil es obligatorio");
        this.nacionalidad = Objects.requireNonNull(nacionalidad, "nacionalidad es obligatoria");
    }

    public String getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefonoMovil() {
        return telefonoMovil;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente cliente)) return false;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
