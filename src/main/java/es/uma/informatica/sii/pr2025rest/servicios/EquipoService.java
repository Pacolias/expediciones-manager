package es.uma.informatica.sii.pr2025rest.servicios;


import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.excepciones.EntidadNoExisteException;
import es.uma.informatica.sii.pr2025rest.excepciones.ExpedicionConEquiposException;
import es.uma.informatica.sii.pr2025rest.repositorios.EquipoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipoService {
    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    public List<Equipo> obtenerTodos() {
        return equipoRepository.findAll();
    }

    public Equipo crearEquipo(Equipo equipo){
        return equipoRepository.save(equipo);
    }

    public Optional<Equipo> consultaEquipoPorID(Long id) {
        return equipoRepository.findById(id);
    }

    public Equipo actualizarEquipo(Equipo equipo) {
        if (!equipoRepository.existsById(equipo.getId()))
            throw new EntidadNoExisteException();

        return equipoRepository.save(equipo);
    }

    public void borrarEquipoPorId(Long id) {
        // Verificar si el equipo existe
        Optional<Equipo> equipoExistente = equipoRepository.findById(id);
        if (!equipoExistente.isPresent()) {
            throw new EntidadNoExisteException(); // Lanzar excepción si no se encuentra el equipo
        }

        // Obtener el equipo y sus expediciones asociadas
        Equipo equipo = equipoExistente.get();

        // Eliminar la asociación con las expediciones
        List<Expedicion> expediciones = equipo.getExpediciones();
        if (expediciones != null && !expediciones.isEmpty()) {
            for (Expedicion expedicion : expediciones) {
                expedicion.getEquipos().remove(equipo);  // Eliminar el equipo de las expediciones
            }
        }

        // Eliminar el equipo de la base de datos
        equipoRepository.deleteById(id);
    }
}