package com.fsocial.postservice.util;

import com.fsocial.postservice.config.DefaultMediaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultMediaProvider {

    private final DefaultMediaConfig config;

    public String pickAvatar(String seed) {
        return pick(config.getAvatars(), seed);
    }

    public String pickBackground(String seed) {
        return pick(config.getBackgrounds(), seed);
    }

    private String pick(List<String> list, String seed) {
        if (list == null || list.isEmpty()) return null;
        int index = Math.abs(seed.hashCode()) % list.size();
        return list.get(index);
    }
}
