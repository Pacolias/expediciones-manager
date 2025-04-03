package es.uma.informatica.sii.pr2025rest.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.List;

@Entity
@Data
public class Expedicion {
    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Equipo> equipos;
}