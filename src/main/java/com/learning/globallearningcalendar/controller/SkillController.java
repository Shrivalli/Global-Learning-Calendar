package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.SkillDTO;
import com.learning.globallearningcalendar.service.ISkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Skill", description = "Skill management APIs")
public class SkillController {

    private final ISkillService skillService;

    @GetMapping
    @Operation(summary = "Get all skills")
    public ResponseEntity<List<SkillDTO>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active skills")
    public ResponseEntity<List<SkillDTO>> getActiveSkills() {
        return ResponseEntity.ok(skillService.getActiveSkills());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID")
    public ResponseEntity<SkillDTO> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get skill by code")
    public ResponseEntity<SkillDTO> getSkillByCode(@PathVariable String code) {
        return ResponseEntity.ok(skillService.getSkillByCode(code));
    }

    @PostMapping
    @Operation(summary = "Create a new skill")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillDTO dto) {
        return new ResponseEntity<>(skillService.createSkill(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a skill")
    public ResponseEntity<SkillDTO> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillDTO dto) {
        return ResponseEntity.ok(skillService.updateSkill(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skill (soft delete)")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get skills by category")
    public ResponseEntity<List<SkillDTO>> getSkillsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(skillService.getSkillsByCategory(category));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all skill categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(skillService.getAllCategories());
    }

    @GetMapping("/search")
    @Operation(summary = "Search skills by name")
    public ResponseEntity<List<SkillDTO>> searchSkills(@RequestParam String searchTerm) {
        return ResponseEntity.ok(skillService.searchSkills(searchTerm));
    }
}
