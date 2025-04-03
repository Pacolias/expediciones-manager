package es.uma.informatica.sii.pr2025rest.repositorios;

import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
}