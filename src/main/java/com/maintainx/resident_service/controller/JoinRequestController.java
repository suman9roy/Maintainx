package com.maintainx.resident_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maintainx.resident_service.dto.JoinRequestDto;
import com.maintainx.resident_service.dto.RejectRequestDto;
import com.maintainx.resident_service.entity.ResidentJoinRequest;
import com.maintainx.resident_service.enums.JoinRequestStatus;
import com.maintainx.resident_service.exception.ResourceNotFoundException;
import com.maintainx.resident_service.service.JoinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService service;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResidentJoinRequest> submit(
            @RequestPart("data")                       String dataJson,
            @RequestPart(value = "document",
                    required = false)             MultipartFile document,
            @RequestHeader("X-User-Id")                String userId) throws IOException {

        // Manually validate after deserializing the JSON part —
        // @Valid doesn't apply to @RequestPart String, so we validate
        // the parsed DTO using javax.validation via the service.
        JoinRequestDto dto = objectMapper.readValue(dataJson, JoinRequestDto.class);

        ResidentJoinRequest saved = service.submitRequest(
                UUID.fromString(userId), dto, document
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/my")
    public List<ResidentJoinRequest> getMyRequests(
            @RequestHeader("X-User-Id") String userId) {

        return service.getMyRequests(UUID.fromString(userId));
    }

    @GetMapping
    public List<ResidentJoinRequest> getAllRequestsByStatus(
            @RequestParam(required = false) JoinRequestStatus status) {

        return service.getAllRequestsByStatus(status);
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) throws IOException {

        ResidentJoinRequest joinRequest = service.getRequestById(id);

        byte[] pdf = service.getDocument(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + joinRequest.getDocumentName() + "\"")
                .body(pdf);
    }

    @PutMapping("/{id}/approve")
    public ResidentJoinRequest approve(@PathVariable Long id) {
        return service.approveRequest(id);
    }

    /**
     * @Valid applied to RejectRequestDto ensures reason is never blank —
     * a resident deserves to know why their request was rejected.
     */
    @PutMapping("/{id}/reject")
    public ResidentJoinRequest reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequestDto dto) {

        return service.rejectRequest(id, dto);
    }
}