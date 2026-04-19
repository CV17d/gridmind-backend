package com.gridmind.backend.controller;

import com.gridmind.backend.model.Project;
import com.gridmind.backend.service.ProjectService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project createProject(
            @RequestBody Project project,
            Authentication authentication
    ) {

        String email = authentication.getName().toLowerCase();

        project.setOwnerEmail(email);

        return projectService.createProject(project);
    }

    @GetMapping
    public List<Project> getProjects(Authentication authentication) {

        String email = authentication.getName().toLowerCase();

        return projectService.getProjectsByOwner(email);
    }

    @GetMapping("/{id}")
    public Project getProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
    
        String email = authentication.getName();
    
        return projectService.getProjectById(id, email);
    }
    
    @DeleteMapping("/{id}")
    public void deleteProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
    
        String email = authentication.getName();
    
        projectService.deleteProject(id, email);
    }

    @PutMapping("/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestBody Project project,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return projectService.updateProject(id, project, email);
    }
  
} 