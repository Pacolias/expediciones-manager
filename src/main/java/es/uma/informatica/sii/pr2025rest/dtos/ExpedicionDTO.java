package es.uma.informatica.sii.pr2025rest.dtos;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class ExpedicionDTO {
    private Long id;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;
    private List<EquipoDTO> equipos;
}
