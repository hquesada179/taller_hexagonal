package com.stagepass.live.domain.model;

import java.util.Objects;

/**
 * Lugar fisico donde se celebran los eventos. El aforo es el limite
 * maximo de asientos que el recinto puede alojar.
 */
public class Recinto {

    private final String id;
    private final String nombre;
    private final int aforo;

    public Recinto(String id, String nombre, int aforo) {
        this.id = Objects.requireNonNull(id, "id es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
        if (aforo <= 0) {
            throw new IllegalArgumentException("El aforo debe ser mayor que cero");
        }
        this.aforo = aforo;
    }

    public boolean tieneCapacidad(int ocupacionActual) {
        return ocupacionActual < aforo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getAforo() {
        return aforo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recinto recinto)) return false;
        return Objects.equals(id, recinto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
