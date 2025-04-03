package es.uma.informatica.sii.pr2025rest.controladores;

import es.uma.informatica.sii.pr2025rest.dtos.EquipoDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionDTO;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionEntradaDTO;
import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {
    public static Expedicion entidad(ExpedicionEntradaDTO dto) {
        Expedicion expedicion = new Expedicion();
        expedicion.setNombre(dto.getNombre());
        expedicion.setFechaInicio(dto.getFechaInicio());
        expedicion.setFechaFin(dto.getFechaFin());
        List<Long> equipos = dto.getEquipos();
        if (equipos == null) {
            equipos = Collections.EMPTY_LIST;
        }
        expedicion.setEquipos(equipos.stream().map(Equipo::new).collect(Collectors.toList()));
        return expedicion;
    }

    public static ExpedicionDTO dto(Expedicion entidad) {
        ExpedicionDTO expedicion = new ExpedicionDTO();
        expedicion.setId(entidad.getId());
        expedicion.setNombre(entidad.getNombre());
        expedicion.setFechaInicio(entidad.getFechaInicio());
        expedicion.setFechaFin(entidad.getFechaFin());
        List<Equipo> equipos = entidad.getEquipos();
        if (equipos == null) {
            equipos = Collections.EMPTY_LIST;
        }
        expedicion.setEquipos(equipos.stream().map(Mapper::dto).collect(Collectors.toList()));
        return expedicion;
    }

    public static EquipoDTO dto(Equipo equipo) {
        EquipoDTO equipoDTO = new EquipoDTO();
        equipoDTO.setNombre(equipo.getNombre());
        equipoDTO.setId(equipo.getId());
        equipoDTO.setEspecialidad(equipo.getEspecialidad());

        return equipoDTO;
    }

    public static Equipo entidad(EquipoDTO dto) {
        Equipo equipo = new Equipo();
        equipo.setNombre(dto.getNombre());
        equipo.setId(dto.getId());
        equipo.setEspecialidad(dto.getEspecialidad());
        return equipo;
    }
}
