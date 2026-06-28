package com.maintainx.resident_service.service;

import com.maintainx.resident_service.dto.JoinRequestDto;
import com.maintainx.resident_service.dto.RejectRequestDto;
import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.entity.ResidentJoinRequest;
import com.maintainx.resident_service.enums.JoinRequestStatus;
import com.maintainx.resident_service.enums.ResidentType;
import com.maintainx.resident_service.exception.DuplicateResourceException;
import com.maintainx.resident_service.exception.InvalidRequestException;
import com.maintainx.resident_service.exception.ResourceNotFoundException;
import com.maintainx.resident_service.kafka.JoinRequestEventProducer;
import com.maintainx.resident_service.kafka.JoinRequestStatusEvent;
import com.maintainx.resident_service.repository.ResidentJoinRequestRepository;
import com.maintainx.resident_service.repository.ResidentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final ResidentJoinRequestRepository joinRequestRepository;
    private final ResidentRepository residentRepository;
    private final JoinRequestEventProducer eventProducer;

    @Value("${document.upload-dir}")
    private String uploadDir;

    // ── RESIDENT: submit ──────────────────────────────────────────────────────

    public ResidentJoinRequest submitRequest(
            UUID userId, JoinRequestDto dto, MultipartFile document) throws IOException {

        // Duplicate pending request check → 409
        if (joinRequestRepository.existsByUserIdAndFlatNumberAndStatus(
                userId, dto.getFlatNumber(), JoinRequestStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "You already have a pending request for flat: " + dto.getFlatNumber()
            );
        }

        // OWNER/TENANT must upload a document → 400
        if (dto.getResidentType() != ResidentType.FAMILY_MEMBER
                && (document == null || document.isEmpty())) {
            throw new InvalidRequestException(
                    "Document is required for " + dto.getResidentType()
                            + ". Please upload your flat deed or rental agreement."
            );
        }

        String documentPath = null;
        String documentName = null;

        if (document != null && !document.isEmpty()) {

            // Wrong file type → 400
            if (!"application/pdf".equals(document.getContentType())) {
                throw new InvalidRequestException(
                        "Only PDF files are accepted. Received: " + document.getContentType()
                );
            }

            // File too large → 400 (also caught at servlet level by MaxUploadSizeExceededException)
            if (document.getSize() > 5 * 1024 * 1024) {
                throw new InvalidRequestException("Document must be under 5MB.");
            }
            log.info("Upload Directory = {}", uploadDir);
            documentPath = storeDocument(userId, document);
            documentName = document.getOriginalFilename();
        }

        ResidentJoinRequest request = ResidentJoinRequest.builder()
                .userId(userId)
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .residentEmail(dto.getResidentEmail())
                .flatNumber(dto.getFlatNumber())
                .blockName(dto.getBlockName())
                .floorNumber(dto.getFloorNumber())
                .residentType(dto.getResidentType())
                .documentName(documentName)
                .documentPath(documentPath)
                .status(JoinRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return joinRequestRepository.save(request);
    }

    // ── RESIDENT: view own ────────────────────────────────────────────────────

    public List<ResidentJoinRequest> getMyRequests(UUID userId) {
        return joinRequestRepository.findByUserId(userId);
    }

    // ── ADMIN: list all ───────────────────────────────────────────────────────

    public List<ResidentJoinRequest> getAllRequestsByStatus(JoinRequestStatus status) {
        // Fixed NPE: status was null when no ?status= param given,
        // and the old code called status.equals() on a null receiver.
        if (status == null || status == JoinRequestStatus.ALL) {
            return joinRequestRepository.findAll();
        }
        return joinRequestRepository.findByStatus(status);
    }

    // ── ADMIN: fetch single request (used by getDocument) ────────────────────

    public ResidentJoinRequest getRequestById(Long id) {
        return joinRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Join request not found with id: " + id
                ));
    }

    // ── ADMIN: download document ──────────────────────────────────────────────

    public byte[] getDocument(Long requestId) throws IOException {

        ResidentJoinRequest joinRequest = getRequestById(requestId);

        if (joinRequest.getDocumentPath() == null) {
            throw new InvalidRequestException(
                    "No document was uploaded for this request."
            );
        }

        Path filePath = Paths.get(joinRequest.getDocumentPath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException(
                    "Document file not found on server. It may have been moved or deleted."
            );
        }

        return Files.readAllBytes(filePath);
    }

    // ── ADMIN: approve ────────────────────────────────────────────────────────

    public ResidentJoinRequest approveRequest(Long requestId) {

        ResidentJoinRequest joinRequest = getRequestById(requestId);

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new InvalidRequestException(
                    "Cannot approve — request is already " + joinRequest.getStatus()
            );
        }

        if (joinRequest.getResidentType() != ResidentType.FAMILY_MEMBER) {
            boolean alreadyExists = residentRepository.existsByFlatNumberAndResidentType(
                    joinRequest.getFlatNumber(), joinRequest.getResidentType()
            );
            if (alreadyExists) {
                throw new DuplicateResourceException(
                        "A " + joinRequest.getResidentType()
                                + " already exists for flat: " + joinRequest.getFlatNumber()
                                + ". Reject this request or remove the existing record first."
                );
            }
        }

        Resident resident = Resident.builder()
                .userId(joinRequest.getUserId())
                .fullName(joinRequest.getFullName())
                .phoneNumber(joinRequest.getPhoneNumber())
                .email(joinRequest.getResidentEmail())
                .flatNumber(joinRequest.getFlatNumber())
                .blockName(joinRequest.getBlockName())
                .floorNumber(joinRequest.getFloorNumber())
                .residentType(joinRequest.getResidentType())
                .joinRequestId(joinRequest.getId())
                .build();

        residentRepository.save(resident);

        joinRequest.setStatus(JoinRequestStatus.APPROVED);
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(joinRequest);

        eventProducer.publishStatusUpdate(
                JoinRequestStatusEvent.builder()
                        .userId(joinRequest.getUserId().toString())
                        .fullName(joinRequest.getFullName())
                        .flatNumber(joinRequest.getFlatNumber())
                        .residentEmail(joinRequest.getResidentEmail())
                        .status("APPROVED")
                        .build()
        );

        log.info("Join request {} approved — resident record created for flat {}",
                requestId, joinRequest.getFlatNumber());

        return joinRequest;
    }

    // ── ADMIN: reject ─────────────────────────────────────────────────────────

    public ResidentJoinRequest rejectRequest(Long requestId, RejectRequestDto dto) {

        ResidentJoinRequest joinRequest = getRequestById(requestId);

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new InvalidRequestException(
                    "Cannot reject — request is already " + joinRequest.getStatus()
            );
        }

        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        joinRequest.setRejectionReason(dto.getReason());
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(joinRequest);

        eventProducer.publishStatusUpdate(
                JoinRequestStatusEvent.builder()
                        .userId(joinRequest.getUserId().toString())
                        .fullName(joinRequest.getFullName())
                        .residentEmail(joinRequest.getResidentEmail())
                        .flatNumber(joinRequest.getFlatNumber())
                        .status("REJECTED")
                        .rejectionReason(dto.getReason())
                        .build()
        );

        log.info("Join request {} rejected — reason: {}", requestId, dto.getReason());

        return joinRequest;
    }

    // ── private: file storage ─────────────────────────────────────────────────

    private String storeDocument(UUID userId, MultipartFile file) throws IOException {

        Path userDir = Paths.get(uploadDir, userId.toString());
        Files.createDirectories(userDir);

        String uniqueFilename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path destination = userDir.resolve(uniqueFilename);

        file.transferTo(destination.toFile());

        log.info("Document stored at: {}", destination);
        return destination.toString();
    }
}