package com.gridmind.backend.service;

import com.gridmind.backend.model.Project;
import com.gridmind.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getProjectsByOwner(String email) {
        return projectRepository.findByOwnerEmail(email);
    }

    public Project getProjectById(Long id, String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Access denied");
        }

        return project;
    }

    public void deleteProject(Long id, String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Access denied");
        }

        projectRepository.delete(project);
    }
    public Project updateProject(Long id, Project updatedProject, String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    
        if (!project.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Access denied");
        }
    
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
    
        return projectRepository.save(project);
    }
}