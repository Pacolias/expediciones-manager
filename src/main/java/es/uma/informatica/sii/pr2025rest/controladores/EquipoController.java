package es.uma.informatica.sii.pr2025rest.controladores;

import es.uma.informatica.sii.pr2025rest.dtos.EquipoDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionEntradaDTO;
import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.excepciones.EntidadNoExisteException;
import es.uma.informatica.sii.pr2025rest.excepciones.ExpedicionConEquiposException;
import es.uma.informatica.sii.pr2025rest.servicios.EquipoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public List<EquipoDTO> obtenerTodos() {
        return equipoService.obtenerTodos().stream()
                .map(Mapper::dto)
                .collect(Collectors.toList());
    }

    // TODO

    @PostMapping
    public ResponseEntity<EquipoDTO> Crear(@RequestBody EquipoDTO equipoDTO, UriComponentsBuilder uriBuilder){
        Equipo equipoMapeado = Mapper.entidad(equipoDTO);
        Equipo nuevoEquipo = equipoService.crearEquipo(equipoMapeado);
        EquipoDTO equipoRespuesta = Mapper.dto(nuevoEquipo);

        URI uri = uriBuilder.path("/equipos/{id}")
                .buildAndExpand(equipoRespuesta.getId())
                .toUri();

        return ResponseEntity.created(uri).body(equipoRespuesta);
    }

    @GetMapping("{id}")
    public ResponseEntity<EquipoDTO> consultarEquipo(@PathVariable Long id){
        return equipoService.consultaEquipoPorID(id)
                .map(exp -> ResponseEntity.ok(Mapper.dto(exp)))      // Si existe, mapea a DTO
                .orElseGet(() -> ResponseEntity.notFound().build()); // Si no existe, devuelve 404
    }

    @PutMapping("{id}")
    public ResponseEntity<Equipo> actualizarEquipo(@PathVariable Long id, @RequestBody EquipoDTO equipoDTO){
        Equipo equipo = Mapper.entidad(equipoDTO);
        equipo.setId(id);

        try {
            Equipo equipoActualizado = equipoService.actualizarEquipo(equipo);
            return ResponseEntity.ok(equipoActualizado);

        } catch(EntidadNoExisteException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Expedicion> borrarEquipo(@PathVariable Long id){
        try{
            equipoService.borrarEquipoPorId(id);
            return ResponseEntity.noContent().build();

        } catch(EntidadNoExisteException e){
            return ResponseEntity.notFound().build();
        }
    }
}