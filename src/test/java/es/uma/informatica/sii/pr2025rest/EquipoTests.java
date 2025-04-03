package es.uma.informatica.sii.pr2025rest;

import es.uma.informatica.sii.pr2025rest.dtos.EquipoDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionEntradaDTO;
import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import es.uma.informatica.sii.pr2025rest.repositorios.EquipoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.repositorios.ExpedicionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("En el servicio de expediciones")
class EquipoTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private ExpedicionRepository expedicionRepository;

    private Equipo equipo;
    private Expedicion expedicion;

    @BeforeEach
    void setUp() {
        equipoRepository.deleteAll();
        expedicionRepository.deleteAll();
    }

    private void compruebaAtributos(Equipo equipo, EquipoDTO dto) {
        assertThat(dto.getNombre()).isEqualTo(equipo.getNombre());
        assertThat(dto.getEspecialidad()).isEqualTo(equipo.getEspecialidad());
    }

    @Nested
    @DisplayName("cuando no hay equipos")
    public class SinEquipos {

        @Test
        @DisplayName("devuelve una lista vacía de equipos")
        void todoEquipos() {
            ResponseEntity<EquipoDTO[]> response = restTemplate.getForEntity("/equipos", EquipoDTO[].class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("devuelve error cuando se intenta acceder a un equipo que no existe")
        void obtenerPorIdNoExiste() {
            ResponseEntity<EquipoDTO> response = restTemplate.getForEntity("/equipos/999", EquipoDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("puede crear un equipo nuevo")
        void crearEquipo() {
            EquipoDTO nuevoEquipo = new EquipoDTO();
            nuevoEquipo.setNombre("Equipo B");
            nuevoEquipo.setEspecialidad("Informática");
            ResponseEntity<EquipoDTO> response = restTemplate.postForEntity("/equipos", nuevoEquipo, EquipoDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo("Equipo B");
            assertThat(response.getHeaders().get("Location").get(0))
                .endsWith("/"+response.getBody().getId());

            Optional<Equipo> equipoGuardado = equipoRepository.findById(response.getBody().getId());
            assertThat(equipoGuardado).isPresent();
            equipoGuardado.ifPresent(equipo -> {
                compruebaAtributos(equipo, nuevoEquipo);
            });
        }

        @Test
        @DisplayName("devuelve error al intentar modifiar un equipo que no existe")
        void actualizarEquipoNoExiste() {
            EquipoDTO equipoActualizado = new EquipoDTO();
            equipoActualizado.setNombre("Equipo B");
            equipoActualizado.setEspecialidad("Informática");
            var peticion = RequestEntity.put("/equipos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .body(equipoActualizado);

            ResponseEntity<EquipoDTO> response = restTemplate.exchange(peticion, EquipoDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("cuando hay equipos")
    public class ConEquipos {

        @BeforeEach
        void setUp() {
            equipo = new Equipo();
            equipo.setNombre("Equipo A");
            equipo.setEspecialidad("Informática");
            equipo = equipoRepository.save(equipo);

            expedicion = new Expedicion();
            expedicion.setNombre("Expedición Everest");
            expedicion.setFechaInicio(Date.valueOf(LocalDate.of(2025,6,10)));
            expedicion.setFechaFin(Date.valueOf(LocalDate.of(2026,6,10)));
            expedicion.setEquipos(List.of(equipo));
            expedicion = expedicionRepository.save(expedicion);
        }

        @Test
        @DisplayName("se obtienen todos los equipos")
        void obtenerTodos() {
            ResponseEntity<Equipo[]> response = restTemplate.getForEntity("/equipos", Equipo[].class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
            assertThat(response.getBody()[0].getNombre()).isEqualTo("Equipo A");
        }

        @Test
        @DisplayName("se devuelve un equipo cuando solo se pregunta por él")
        void obtenerPorIdExiste() {
            ResponseEntity<Equipo> response = restTemplate.getForEntity("/equipos/" + equipo.getId(), Equipo.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo("Equipo A");
        }

        @Test
        @DisplayName("se actualiza un equipo cuando existe")
        void actualizarEquipoExiste() {
            EquipoDTO equipoActualizado = new EquipoDTO();
            equipoActualizado.setNombre("Equipo X");
            equipoActualizado.setEspecialidad("Biología");
            var peticion = RequestEntity.put("/equipos/"+equipo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(equipoActualizado);

            ResponseEntity<EquipoDTO> response = restTemplate.exchange(peticion, EquipoDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo("Equipo X");

            Optional<Equipo> equipoGuardado = equipoRepository.findById(equipo.getId());
            assertThat(equipoGuardado).isPresent();
            assertThat(equipoGuardado.get().getNombre()).isEqualTo("Equipo X");
        }

        @Test
        @DisplayName("se puede eliminar un equipo")
        void eliminarEquipo() {
            ResponseEntity<Void> response = restTemplate.exchange("/equipos/" + equipo.getId(), HttpMethod.DELETE, null, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(equipoRepository.findById(equipo.getId())).isEmpty();
        }


    }


}