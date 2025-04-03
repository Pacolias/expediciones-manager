package es.uma.informatica.sii.pr2025rest.servicios;

import es.uma.informatica.sii.pr2025rest.controladores.Mapper;
import es.uma.informatica.sii.pr2025rest.dtos.ExpedicionDTO;
import es.uma.informatica.sii.pr2025rest.entidades.Equipo;
import es.uma.informatica.sii.pr2025rest.entidades.Expedicion;
import es.uma.informatica.sii.pr2025rest.excepciones.EntidadNoExisteException;
import es.uma.informatica.sii.pr2025rest.excepciones.ExpedicionConEquiposException;
import es.uma.informatica.sii.pr2025rest.repositorios.EquipoRepository;
import es.uma.informatica.sii.pr2025rest.repositorios.ExpedicionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpedicionService {

    private final ExpedicionRepository expedicionRepository;
    private final EquipoRepository equipoRepository;

    public ExpedicionService(ExpedicionRepository expedicionRepository, EquipoRepository equipoRepository) {
        this.expedicionRepository = expedicionRepository;
        this.equipoRepository = equipoRepository;
    }

    // TODO

    public List<Expedicion> obtenerTodos() {
        return expedicionRepository.findAll();
    }

    public Expedicion nuevaExpedicion(Expedicion expedicion) {
        List<Equipo> listaEquipos = expedicion.getEquipos();
        if (!equipoRepository.findAll().containsAll(listaEquipos))
            throw new ExpedicionConEquiposException();

        return expedicionRepository.save(expedicion);
    }

    public Optional<Expedicion> consultaExpedicionPorID(Long id) {
        return expedicionRepository.findById(id);
    }

    public Expedicion actualizarExpedicion(Expedicion expedicion) {
        if (!expedicionRepository.existsById(expedicion.getId()))
            throw new EntidadNoExisteException();

        if (expedicion.getEquipos() != null) {
            List<Long> idsEquipos = expedicion.getEquipos().stream()
                    .map(Equipo::getId)
                    .collect(Collectors.toList());

            List<Equipo> equiposExistentes = equipoRepository.findAllById(idsEquipos);

            if (equiposExistentes.size() != expedicion.getEquipos().size()) {
                throw new ExpedicionConEquiposException();
            }
        }

        return expedicionRepository.save(expedicion);
    }

    public void borrarExpedicionPorId(Long id){
        Optional<Expedicion> expedicion = expedicionRepository.findById(id);
        if (!expedicion.isPresent()) {
            throw new EntidadNoExisteException(); // Si no existe la expedición
        }

        // Verificar si la expedición tiene equipos asociados
        if (!expedicion.get().getEquipos().isEmpty()) {
            // Si tiene equipos asociados, devolver 403 Forbidden
            throw new ExpedicionConEquiposException(); // Lanza una excepción para devolver 403
        }

        // Eliminar la expedición si no tiene equipos asociados
        expedicionRepository.deleteById(id);
    }
}