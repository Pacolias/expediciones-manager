package es.uma.informatica.sii.pr2025rest;

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
class ExpedicionesTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private ExpedicionRepository expedicionRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @BeforeEach
    public void setUp() {
        expedicionRepository.deleteAll();
    }

    private void compruebaAtributos(Expedicion expedicion, ExpedicionEntradaDTO dto) {
        assertThat(dto.getNombre()).isEqualTo(expedicion.getNombre());
        assertThat(dto.getFechaFin()).isEqualTo(expedicion.getFechaFin());
        assertThat(dto.getFechaInicio()).isEqualTo(expedicion.getFechaInicio());
    }

    @Nested
    @DisplayName("cuando no hay expediciones")
    public class SinExpediciones {

        @Test
        @DisplayName("obtiene una lista de expediciones vacía")
        void obtenerTodas() {
            ResponseEntity<ExpedicionDTO[]> response = restTemplate.getForEntity("/expediciones", ExpedicionDTO[].class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("devuelve error cuando consulta una petición que no existe")
        void obtenerPorIdNoExiste() {
            ResponseEntity<ExpedicionDTO> response = restTemplate.getForEntity("/expediciones/999", ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("devuelve error cuando intenta modificar una petición que no existe")
        void actualizarExpedicion_NoExiste() {
            ExpedicionEntradaDTO expedicionActualizada = new ExpedicionEntradaDTO();
            expedicionActualizada.setNombre("Expedición Fantasma");
            expedicionActualizada.setFechaInicio(Date.valueOf(LocalDate.of(2025, 4, 15)));
            expedicionActualizada.setFechaFin(Date.valueOf(LocalDate.of(2025,6,10)));
            var peticion = RequestEntity.put("/expediciones/999")
                .contentType(MediaType.APPLICATION_JSON)
                .body(expedicionActualizada);


            ResponseEntity<ExpedicionDTO> response = restTemplate.exchange(peticion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("devuelve error al intentar eliminar una expedición que no existe")
        void eliminarExpedicion() {
            ResponseEntity<Void> response = restTemplate.exchange("/expediciones/999", HttpMethod.DELETE, null, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("puede añadir una nueva expedición")
        void crearExpedicion() {
            ExpedicionEntradaDTO nuevaExpedicion = new ExpedicionEntradaDTO();
            nuevaExpedicion.setNombre("Expedición Antártica");
            nuevaExpedicion.setFechaInicio(Date.valueOf(LocalDate.of(2025, 4, 15)));
            nuevaExpedicion.setFechaFin(Date.valueOf(LocalDate.of(2025,6,10)));

            ResponseEntity<ExpedicionDTO> response = restTemplate.postForEntity("/expediciones", nuevaExpedicion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo("Expedición Antártica");
            assertThat(response.getHeaders().get("Location").get(0))
                .endsWith("/"+response.getBody().getId());

            Optional<Expedicion> expedicionGuardada = expedicionRepository.findById(response.getBody().getId());
            assertThat(expedicionGuardada).isPresent();
            expedicionGuardada.ifPresent(expedicion -> {
                compruebaAtributos(expedicion, nuevaExpedicion);
            });
        }

    }

    @Nested
    @DisplayName("cuando hay expediciones")
    public class ConExpedicion {

        private Expedicion expedicionConEquipo;
        private Expedicion expedicionSinEquipo;
        private Equipo equipo;

        @BeforeEach
        void setUp() {
            equipo = new Equipo();
            equipo.setNombre("Equipo A");
            equipo.setEspecialidad("Geología");
            equipo = equipoRepository.save(equipo);

            expedicionConEquipo = new Expedicion();
            expedicionConEquipo.setNombre("Expedición Ártica");
            expedicionConEquipo.setFechaInicio(Date.valueOf(LocalDate.of(2025, 4, 15)));
            expedicionConEquipo.setFechaFin(Date.valueOf(LocalDate.of(2025,6,10)));
            expedicionConEquipo.setEquipos(List.of(equipo));
            expedicionConEquipo = expedicionRepository.save(expedicionConEquipo);

            expedicionSinEquipo = new Expedicion();
            expedicionSinEquipo.setNombre("Expedición Antártica");
            expedicionSinEquipo.setFechaInicio(Date.valueOf(LocalDate.of(2025, 3, 15)));
            expedicionSinEquipo.setFechaFin(Date.valueOf(LocalDate.of(2025,4,10)));
            expedicionSinEquipo = expedicionRepository.save(expedicionSinEquipo);
        }

        @Test
        @DisplayName("se obtienen todas")
        void obtenerTodas() {
            ResponseEntity<Expedicion[]> response = restTemplate.getForEntity("/expediciones", Expedicion[].class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
            assertThat(response.getBody().length).isEqualTo(2);
        }

        @Test
        @DisplayName("se obtiene una expedición cuando se da un id")
        void obtenerPorId_Existe() {
            ResponseEntity<Expedicion> response = restTemplate.getForEntity("/expediciones/" + expedicionConEquipo.getId(), Expedicion.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo(expedicionConEquipo.getNombre());
        }


        @Test
        @DisplayName("se actualizan las expediciones cuando existen")
        void actualizarExpedicionExiste() {
            ExpedicionEntradaDTO expedicionActualizada = new ExpedicionEntradaDTO();
            expedicionActualizada.setNombre("Expedición Renovada");
            expedicionActualizada.setFechaInicio(Date.valueOf(LocalDate.of(2025, 4, 15)));
            expedicionActualizada.setFechaFin(Date.valueOf(LocalDate.of(2025,6,10)));

            var peticion = RequestEntity.put("/expediciones/" + expedicionConEquipo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(expedicionActualizada);


            ResponseEntity<ExpedicionDTO> response = restTemplate.exchange(peticion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNombre()).isEqualTo(expedicionActualizada.getNombre());

            Optional<Expedicion> expedicionGuardada = expedicionRepository.findById(expedicionConEquipo.getId());
            assertThat(expedicionGuardada).isPresent();
            expedicionGuardada.ifPresent(expedicion -> {
                compruebaAtributos(expedicion, expedicionActualizada);
            });
        }


        @Test
        @DisplayName("se puede eliminar una expedición si no tiene equipos")
        void eliminarExpedicionSinEquipo() {
            ResponseEntity<Void> response = restTemplate.exchange("/expediciones/" + expedicionSinEquipo.getId(), HttpMethod.DELETE, null, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(expedicionRepository.findById(expedicionSinEquipo.getId())).isEmpty();
        }

        @Test
        @DisplayName("devuelve error cuando se intenta eliminar que tiene equipos")
        void eliminarExpedicionConEquipo() {
            ResponseEntity<Void> response = restTemplate.exchange("/expediciones/" + expedicionConEquipo.getId(), HttpMethod.DELETE, null, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(expedicionRepository.findById(expedicionConEquipo.getId())).isNotEmpty();
        }

        @Test
        @DisplayName("se puede asignar un equipo a una expedición si existe el equipo")
        void asignarEquipoAExpedicion() {
            ExpedicionEntradaDTO expedicionActualizada = new ExpedicionEntradaDTO();
            expedicionActualizada.setNombre(expedicionSinEquipo.getNombre());
            expedicionActualizada.setFechaInicio(expedicionSinEquipo.getFechaInicio());
            expedicionActualizada.setFechaFin(expedicionSinEquipo.getFechaFin());
            expedicionActualizada.setEquipos(List.of(equipo.getId()));
            var peticion = RequestEntity.put("/expediciones/" + expedicionSinEquipo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(expedicionActualizada);


            ResponseEntity<ExpedicionDTO> response = restTemplate.exchange(peticion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEquipos()).isNotEmpty();
            assertThat(response.getBody().getEquipos().get(0).getId()).isEqualTo(equipo.getId());

            Optional<Expedicion> expedicionGuardada = expedicionRepository.findById(expedicionSinEquipo.getId());
            assertThat(expedicionGuardada).isPresent();
            expedicionGuardada.ifPresent(expedicion -> {
                assertThat(expedicion.getEquipos()).isNotEmpty();
                assertThat(expedicion.getEquipos().get(0).getId()).isEqualTo(equipo.getId());
            });

        }

        @Test
        @DisplayName("devuelve error cuando se asigna un equipo que no existe a una expedición")
        void asignarEquipoAExpedicionEquipoNoExiste() {
            ExpedicionEntradaDTO expedicionActualizada = new ExpedicionEntradaDTO();
            expedicionActualizada.setNombre(expedicionSinEquipo.getNombre());
            expedicionActualizada.setFechaInicio(expedicionSinEquipo.getFechaInicio());
            expedicionActualizada.setFechaFin(expedicionSinEquipo.getFechaFin());
            expedicionActualizada.setEquipos(List.of(999L));
            var peticion = RequestEntity.put("/expediciones/" + expedicionSinEquipo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(expedicionActualizada);


            ResponseEntity<ExpedicionDTO> response = restTemplate.exchange(peticion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("se puede eliminar un equipo de una expedición")
        void eliminarEquipoDeExpedicion() {
            ExpedicionEntradaDTO expedicionActualizada = new ExpedicionEntradaDTO();
            expedicionActualizada.setNombre(expedicionConEquipo.getNombre());
            expedicionActualizada.setFechaInicio(expedicionConEquipo.getFechaInicio());
            expedicionActualizada.setFechaFin(expedicionConEquipo.getFechaFin());
            expedicionActualizada.setEquipos(List.of());
            var peticion = RequestEntity.put("/expediciones/" + expedicionConEquipo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(expedicionActualizada);


            ResponseEntity<ExpedicionDTO> response = restTemplate.exchange(peticion, ExpedicionDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEquipos()).isEmpty();

            Optional<Expedicion> expedicionGuardada = expedicionRepository.findById(expedicionSinEquipo.getId());
            assertThat(expedicionGuardada).isPresent();
            expedicionGuardada.ifPresent(expedicion -> {
                assertThat(expedicion.getEquipos()).isEmpty();
            });

        }

    }

}
