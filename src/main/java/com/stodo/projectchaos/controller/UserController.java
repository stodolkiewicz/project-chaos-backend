package com.stodo.projectchaos.controller;

import com.stodo.projectchaos.model.dto.project.defaultproject.response.DefaultProjectIdResponseDTO;
import com.stodo.projectchaos.model.dto.user.update.request.ChangeDefaultProjectRequestDTO;
import com.stodo.projectchaos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/default-project")
    public ResponseEntity<DefaultProjectIdResponseDTO> getDefaultProjectId(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<UUID> defaultProjectId = userService.findDefaultProjectIdByEmail(email);

        return ResponseEntity.ok(new DefaultProjectIdResponseDTO(defaultProjectId.orElse(null)));
    }

    @PatchMapping("/default-project")
    public ResponseEntity<Void> changeDefaultProject(
            @Valid @RequestBody ChangeDefaultProjectRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.changeDefaultProject(request, email);
        return ResponseEntity.ok().build();
    }
}
