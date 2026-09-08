package com.example.portfolio.controller;

import com.example.portfolio.config.OwnerProperties;
import com.example.portfolio.domain.Project;
import com.example.portfolio.domain.ProjectKind;
import com.example.portfolio.service.ProjectService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PortfolioController {

    private final ProjectService projectService;
    private final OwnerProperties ownerProperties;

    /** 좌측 레일과 연락처가 모든 화면에 있으므로 공통으로 올린다 */
    @ModelAttribute("owner")
    public OwnerProperties owner() {
        return ownerProperties;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("personalKind", ProjectKind.PERSONAL);
        model.addAttribute("teamKind", ProjectKind.TEAM);
        model.addAttribute("personalProjects", projectService.findByKind(ProjectKind.PERSONAL));
        model.addAttribute("teamProjects", projectService.findByKind(ProjectKind.TEAM));
        model.addAttribute("deployedCount", projectService.countDeployed());
        model.addAttribute("active", "home");
        return "index";
    }

    @GetMapping("/personal")
    public String personal(Model model) {
        return listPage(ProjectKind.PERSONAL, model);
    }

    @GetMapping("/team")
    public String team(Model model) {
        return listPage(ProjectKind.TEAM, model);
    }

    @GetMapping("/projects/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Project project = projectService.getBySlug(slug);
        model.addAttribute("project", project);
        model.addAttribute("active", project.getKind().getPath());
        return "project/detail";
    }

    private String listPage(ProjectKind kind, Model model) {
        List<Project> projects = projectService.findByKind(kind);
        model.addAttribute("kind", kind);
        model.addAttribute("projects", projects);
        // 상단 바로가기 줄용. 거르는 일은 화면이 아니라 여기서 한다
        model.addAttribute("liveProjects", projects.stream().filter(Project::isDeployed).toList());
        model.addAttribute("active", kind.getPath());
        return "project/list";
    }
}
