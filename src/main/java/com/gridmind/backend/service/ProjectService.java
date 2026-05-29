package com.gridmind.backend.service;

import com.gridmind.backend.model.Project;
import com.gridmind.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import com.gridmind.backend.exception.AccessDeniedException;
import com.gridmind.backend.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        log.info("Creando nuevo proyecto: '{}' para el propietario: {}", project.getName(), project.getOwnerEmail());
        return projectRepository.save(project);
    }

    public List<Project> getProjectsByOwner(String email) {
        log.debug("Consultando proyectos para el propietario: {}", email);
        return projectRepository.findByOwnerEmail(email);
    }

    public Project getProjectById(Long id, String email) {
        log.debug("Buscando proyecto ID: {} para el usuario: {}", id, email);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            log.warn("ACCESO DENEGADO: El usuario {} intentó acceder al proyecto {} sin ser el dueño.", email, id);
            throw new AccessDeniedException("No tienes permiso para ver este proyecto");
        }

        return project;
    }

    public void deleteProject(Long id, String email) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            log.warn("ELIMINACIÓN DENEGADA: El usuario {} intentó borrar el proyecto {} sin autorización.", email, id);
            throw new AccessDeniedException("No tienes permiso para eliminar este proyecto");
        }

        log.info("Eliminando proyecto ID: {} ('{}') por solicitud de {}", id, project.getName(), email);
        projectRepository.delete(project);
    }

    public Project updateProject(Long id, Project updatedProject, String email) {
        log.info("Actualizando proyecto ID: {} por usuario: {}", id, email);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
    
        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            log.warn("ACTUALIZACIÓN DENEGADA: El usuario {} intentó modificar el proyecto {} sin permisos.", email, id);
            throw new AccessDeniedException("Access denied");
        }
    
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
    
        return projectRepository.save(project);
    }
}