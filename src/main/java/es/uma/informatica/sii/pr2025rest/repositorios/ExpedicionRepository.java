package es.uma.informatica.sii.pr2025rest.repositorios;

import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpedicionRepository extends JpaRepository<Expedicion, Long> {
}