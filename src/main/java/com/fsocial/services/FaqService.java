package com.fsocial.services;

import com.fsocial.dto.faq.FaqDTO;
import com.fsocial.dto.faq.FaqRatingRequest;
import com.fsocial.dto.faq.FaqRequest;
import com.fsocial.enums.FaqType;

import java.util.List;

public interface FaqService {
    FaqDTO createFaq(FaqRequest request);
    FaqDTO updateFaq(String faqId, FaqRequest request);
    void deleteFaq(String faqId);
    FaqDTO getFaqById(String faqId, String userId);
    List<FaqDTO> getAllFaqs();
    List<FaqDTO> getFaqsByType(FaqType type);
    FaqDTO rateFaq(String faqId, String userId, FaqRatingRequest request);
}
