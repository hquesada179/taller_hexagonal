package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.domain.model.Asiento;
import com.stagepass.live.domain.model.Evento;
import com.stagepass.live.domain.model.Recinto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion REAL contra MySQL (via Testcontainers): demuestra
 * que MySQLCatalogoAdapter lee datos verdaderos de la base de datos, no
 * datos simulados en memoria.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySQLCatalogoAdapter.class)
class MySQLCatalogoAdapterIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("stagepass")
            .withUsername("stagepass_app")
            .withPassword("stagepass_app_pwd")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/mysql-test-init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    @DynamicPropertySource
    static void propiedadesDeConexion(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MySQLCatalogoAdapter adapter;

    @Test
    void leeElRecintoRealDesdeMySQL() {
        Optional<Recinto> recinto = adapter.recinto("recinto-test");

        assertThat(recinto).isPresent();
        assertThat(recinto.get().getNombre()).isEqualTo("Recinto de Prueba");
        assertThat(recinto.get().getAforo()).isEqualTo(100);
    }

    @Test
    void leeElListadoRealDeEventosDesdeMySQL() {
        List<Evento> eventos = adapter.eventos();

        assertThat(eventos).hasSize(1);
        Evento evento = eventos.get(0);
        assertThat(evento.getId()).isEqualTo("evento-test");
        assertThat(evento.estaProgramado()).isTrue();
    }

    @Test
    void leeLosAsientosRealesDelRecintoAsociadosAUnEvento() {
        List<Asiento> asientos = adapter.asientos("evento-test");

        assertThat(asientos).hasSize(2);
        assertThat(asientos).extracting(Asiento::getCodigo).containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    void retornaVacioParaUnEventoQueNoExiste() {
        assertThat(adapter.evento("no-existe")).isEmpty();
        assertThat(adapter.asientos("no-existe")).isEmpty();
    }
}
