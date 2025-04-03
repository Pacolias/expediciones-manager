package es.uma.informatica.sii.pr2025rest.controladores;

import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionEntradaDTO;
import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.excepciones.EntidadNoExisteException;
import es.uma.informatica.sii.pr2025rest.excepciones.ExpedicionConEquiposException;
import es.uma.informatica.sii.pr2025rest.servicios.ExpedicionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/expediciones")
public class ExpedicionController {

    private final ExpedicionService expedicionService;

    public ExpedicionController(ExpedicionService expedicionService) {
        this.expedicionService = expedicionService;
    }

    @GetMapping
    public List<ExpedicionDTO> getListaExpediciones(){
        return expedicionService.obtenerTodos().stream()
                .map(Mapper::dto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Expedicion> crearEntidad(@RequestBody ExpedicionEntradaDTO expedicionDTO, UriComponentsBuilder uriBuilder){
        Expedicion expedicionMapeada = Mapper.entidad(expedicionDTO);
        try{
            Expedicion expedicionRespuesta = expedicionService.nuevaExpedicion(expedicionMapeada);

            URI uri = uriBuilder.path("/expediciones/{id}")
                    .buildAndExpand(expedicionRespuesta.getId())
                    .toUri();

            return ResponseEntity.created(uri).body(expedicionRespuesta);

        } catch(ExpedicionConEquiposException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<ExpedicionDTO> consultaExpedicionID(@PathVariable Long id){
        return expedicionService.consultaExpedicionPorID(id)
                .map(exp -> ResponseEntity.ok(Mapper.dto(exp)))   // Si existe, mapea a DTO
                .orElseGet(() -> ResponseEntity.notFound().build());        // Si no existe, devuelve 404
    }

    @PutMapping("{id}")
    public ResponseEntity<Expedicion> actualizarExpedicion(@PathVariable Long id, @RequestBody ExpedicionEntradaDTO expedicionEntradaDTO){
        Expedicion expedicion = Mapper.entidad(expedicionEntradaDTO);
        expedicion.setId(id);

        try {
            Expedicion expedicionActualizada = expedicionService.actualizarExpedicion(expedicion);
            return ResponseEntity.ok(expedicionActualizada);

        } catch(EntidadNoExisteException e){
            return ResponseEntity.notFound().build();
        } catch(ExpedicionConEquiposException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Expedicion> borrarExpedicion(@PathVariable Long id){
        try{
            expedicionService.borrarExpedicionPorId(id);
            return ResponseEntity.noContent().build();

        } catch(EntidadNoExisteException e){
            return ResponseEntity.notFound().build();
        } catch(ExpedicionConEquiposException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}