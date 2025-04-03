package es.uma.informatica.sii.pr2025rest.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Equipo {
    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private String especialidad;

    @ManyToMany(mappedBy = "equipos")
    private List<Expedicion> expediciones;

    public Equipo(Long id) {
        this.id = id;
    }
}