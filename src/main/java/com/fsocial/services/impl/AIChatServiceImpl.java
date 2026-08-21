package com.fsocial.services.impl;

import com.fsocial.entity.Faq;
import com.fsocial.repository.FaqRepository;
import com.fsocial.services.AIChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIChatServiceImpl implements AIChatService {

    static int MAX_CONTEXT_FAQS = 5;
    static int MIN_TOKEN_LENGTH = 2;

    // ponytail: keyword gate thay vì phân loại intent bằng LLM/embedding.
    // Nâng cấp khi tỉ lệ bỏ sót câu hỏi hỗ trợ trở nên đáng kể.
    static Set<String> SUPPORT_KEYWORDS = Set.of(
            "chinh", "sach", "dieu", "khoan", "quy", "dinh", "bao", "mat", "rieng", "tu",
            "ho", "tro", "huong", "dan", "cach", "lam", "sao", "the", "nao", "loi", "bug",
            "tai", "khau", "dang", "nhap", "ky", "xoa", "khoa", "cam",
            "bai", "viet", "post", "comment", "binh", "luan", "like", "share", "chia", "se",
            "ban", "be", "follow", "theo", "doi", "tin", "nhan", "thong", "faq", "vi", "pham",
            "khieu", "nai", "to", "cao", "app", "ung", "dung", "flowzone"
    );

    ChatClient chatClient;
    FaqRepository faqRepository;

    @Override
    public String chatService(String prompt) {
        String context = buildContext(prompt);
        return chatClient.prompt()
                .system(system -> system.param("context", context))
                .user(prompt)
                .call()
                .content();
    }

    private String buildContext(String prompt) {
        Set<String> keywords = tokens(prompt);
        if (keywords.isEmpty() || keywords.stream().noneMatch(SUPPORT_KEYWORDS::contains)) {
            return "";
        }

        return faqRepository.findAll().stream()
                .map(faq -> new ScoredFaq(faq, score(faq, keywords)))
                .filter(scoredFaq -> scoredFaq.score() > 0)
                .sorted(Comparator.comparingLong(ScoredFaq::score).reversed())
                .limit(MAX_CONTEXT_FAQS)
                .map(scoredFaq -> formatFaq(scoredFaq.faq()))
                .collect(Collectors.joining("\n\n"));
    }

    private long score(Faq faq, Set<String> keywords) {
        String content = normalize(String.join(" ",
                value(faq.getName()),
                value(faq.getDescription()),
                value(faq.getContent()),
                faq.getType().name()
        ));
        return keywords.stream().filter(content::contains).count();
    }

    private Set<String> tokens(String text) {
        return Arrays.stream(normalize(text).split("\\s+"))
                .filter(token -> token.length() >= MIN_TOKEN_LENGTH)
                .collect(Collectors.toSet());
    }

    private String formatFaq(Faq faq) {
        return "Tên: " + value(faq.getName()) + "\n"
                + "Loại: " + faq.getType() + "\n"
                + "Mô tả: " + value(faq.getDescription()) + "\n"
                + "Nội dung: " + value(faq.getContent());
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ");
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private record ScoredFaq(Faq faq, long score) {
    }
}
