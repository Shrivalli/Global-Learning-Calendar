package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.LearningProgramDTO;
import com.learning.globallearningcalendar.entity.LearningProgram;
import com.learning.globallearningcalendar.service.ILearningProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-programs")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Learning Program", description = "Learning Program management APIs")
public class LearningProgramController {

    private final ILearningProgramService learningProgramService;

    @GetMapping
    @Operation(summary = "Get all learning programs")
    public ResponseEntity<List<LearningProgramDTO>> getAllPrograms() {
        return ResponseEntity.ok(learningProgramService.getAllPrograms());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all learning programs with pagination")
    public ResponseEntity<Page<LearningProgramDTO>> getAllProgramsPaged(Pageable pageable) {
        return ResponseEntity.ok(learningProgramService.getAllPrograms(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active learning programs")
    public ResponseEntity<List<LearningProgramDTO>> getActivePrograms() {
        return ResponseEntity.ok(learningProgramService.getActivePrograms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get learning program by ID")
    public ResponseEntity<LearningProgramDTO> getProgramById(@PathVariable Long id) {
        return ResponseEntity.ok(learningProgramService.getProgramById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get learning program by code")
    public ResponseEntity<LearningProgramDTO> getProgramByCode(@PathVariable String code) {
        return ResponseEntity.ok(learningProgramService.getProgramByCode(code));
    }

    @PostMapping
    @Operation(summary = "Create a new learning program")
    public ResponseEntity<LearningProgramDTO> createProgram(@Valid @RequestBody LearningProgramDTO dto) {
        return new ResponseEntity<>(learningProgramService.createProgram(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a learning program")
    public ResponseEntity<LearningProgramDTO> updateProgram(@PathVariable Long id, @Valid @RequestBody LearningProgramDTO dto) {
        return ResponseEntity.ok(learningProgramService.updateProgram(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a learning program (soft delete)")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        learningProgramService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{programType}")
    @Operation(summary = "Get learning programs by type")
    public ResponseEntity<List<LearningProgramDTO>> getProgramsByType(@PathVariable LearningProgram.ProgramType programType) {
        return ResponseEntity.ok(learningProgramService.getProgramsByType(programType));
    }

    @GetMapping("/delivery-mode/{deliveryMode}")
    @Operation(summary = "Get learning programs by delivery mode")
    public ResponseEntity<List<LearningProgramDTO>> getProgramsByDeliveryMode(@PathVariable LearningProgram.DeliveryMode deliveryMode) {
        return ResponseEntity.ok(learningProgramService.getProgramsByDeliveryMode(deliveryMode));
    }

    @GetMapping("/mandatory")
    @Operation(summary = "Get mandatory learning programs")
    public ResponseEntity<List<LearningProgramDTO>> getMandatoryPrograms() {
        return ResponseEntity.ok(learningProgramService.getMandatoryPrograms());
    }

    @GetMapping("/skill/{skillId}")
    @Operation(summary = "Get learning programs by skill")
    public ResponseEntity<List<LearningProgramDTO>> getProgramsBySkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(learningProgramService.getProgramsBySkill(skillId));
    }

    @GetMapping("/target-role/{roleId}")
    @Operation(summary = "Get learning programs by target role")
    public ResponseEntity<List<LearningProgramDTO>> getProgramsByTargetRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(learningProgramService.getProgramsByTargetRole(roleId));
    }

    @GetMapping("/target-business-unit/{buId}")
    @Operation(summary = "Get learning programs by target business unit")
    public ResponseEntity<List<LearningProgramDTO>> getProgramsByTargetBusinessUnit(@PathVariable Long buId) {
        return ResponseEntity.ok(learningProgramService.getProgramsByTargetBusinessUnit(buId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search learning programs")
    public ResponseEntity<List<LearningProgramDTO>> searchPrograms(@RequestParam String searchTerm) {
        return ResponseEntity.ok(learningProgramService.searchPrograms(searchTerm));
    }

    @GetMapping("/{id}/session-count")
    @Operation(summary = "Get session count for a learning program")
    public ResponseEntity<Long> countSessionsByProgram(@PathVariable Long id) {
        return ResponseEntity.ok(learningProgramService.countSessionsByProgram(id));
    }
}
