package com.fsocial.services.impl;

import com.fsocial.dto.faq.FaqDTO;
import com.fsocial.dto.faq.FaqRatingRequest;
import com.fsocial.dto.faq.FaqRatingStatsDTO;
import com.fsocial.dto.faq.FaqRequest;
import com.fsocial.entity.Attachments;
import com.fsocial.entity.Faq;
import com.fsocial.entity.FaqRating;
import com.fsocial.entity.FaqView;
import com.fsocial.enums.FaqType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.mapper.FaqMapper;
import com.fsocial.repository.AttachmentsRepository;
import com.fsocial.repository.FaqRatingRepository;
import com.fsocial.repository.FaqRepository;
import com.fsocial.repository.FaqViewRepository;
import com.fsocial.services.FaqService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    FaqRepository faqRepository;
    FaqViewRepository faqViewRepository;
    FaqRatingRepository faqRatingRepository;
    AttachmentsRepository attachmentsRepository;
    FaqMapper faqMapper;

    @Override
    public FaqDTO createFaq(FaqRequest request) {
        Faq faq = faqMapper.toEntity(request);
        faq.setAttachment(resolveAttachment(request.getAttachmentId()));
        return enrich(faqRepository.save(faq));
    }

    @Override
    public FaqDTO updateFaq(String faqId, FaqRequest request) {
        Faq faq = findFaqOrThrow(faqId);
        faq.setName(request.getName());
        faq.setDescription(request.getDescription());
        faq.setContent(request.getContent());
        faq.setType(request.getType());
        faq.setAttachment(resolveAttachment(request.getAttachmentId()));
        return enrich(faqRepository.save(faq));
    }

    @Override
    @Transactional
    public void deleteFaq(String faqId) {
        findFaqOrThrow(faqId);
        faqRatingRepository.deleteByFaqId(faqId);
        faqViewRepository.deleteByFaqId(faqId);
        faqRepository.deleteById(faqId);
    }

    @Override
    public FaqDTO getFaqById(String faqId, String userId) {
        Faq faq = findFaqOrThrow(faqId);
        faqViewRepository.save(FaqView.builder().faqId(faqId).userId(userId).build());
        return enrich(faq);
    }

    @Override
    public List<FaqDTO> getAllFaqs() {
        return faqMapper.toListDTO(faqRepository.findAll()).stream()
                .map(this::withStats)
                .toList();
    }

    @Override
    public List<FaqDTO> getFaqsByType(FaqType type) {
        return faqMapper.toListDTO(faqRepository.findByType(type)).stream()
                .map(this::withStats)
                .toList();
    }

    @Override
    public FaqDTO rateFaq(String faqId, String userId, FaqRatingRequest request) {
        findFaqOrThrow(faqId);
        FaqRating rating = faqRatingRepository.findByFaqIdAndUserId(faqId, userId)
                .orElseGet(() -> FaqRating.builder().faqId(faqId).userId(userId).build());
        rating.setScore(request.getScore());
        rating.setUpdatedAt(LocalDateTime.now());
        faqRatingRepository.save(rating);
        return enrich(findFaqOrThrow(faqId));
    }

    private Faq findFaqOrThrow(String faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new AppException(StatusCode.FAQ_NOT_FOUND));
    }

    private Attachments resolveAttachment(String attachmentId) {
        if (attachmentId == null || attachmentId.isBlank()) {
            return null;
        }
        return attachmentsRepository.findById(attachmentId)
                .orElseThrow(() -> new AppException(StatusCode.FILE_NOT_FOUND));
    }

    private FaqDTO enrich(Faq faq) {
        return withStats(faqMapper.toDTO(faq));
    }

    private FaqDTO withStats(FaqDTO dto) {
        FaqRatingStatsDTO stats = faqRatingRepository.getStats(dto.getId());
        dto.setViewCount(faqViewRepository.countByFaqId(dto.getId()));
        dto.setRatingCount(stats.getRatingCount());
        dto.setAverageRating(stats.getAverageRating());
        return dto;
    }
}
