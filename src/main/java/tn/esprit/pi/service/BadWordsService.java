package tn.esprit.pi.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Detects and masks offensive words in text.
 * Word lists cover English, French, and Arabic.
 */
@Service
public class BadWordsService {

    // ── Word list (English / French / Arabic) ─────────────────────────────────
    private static final List<String> BAD_WORDS = List.of(
            // English
            "fuck", "shit", "asshole", "bitch", "bastard", "cunt", "dick",
            "pussy", "cock", "whore", "slut", "faggot", "nigger", "nigga",
            "retard", "idiot", "moron", "imbecile", "stupid", "dumbass",
            "motherfucker", "bullshit", "crap", "damn", "hell", "piss",
            "wanker", "twat", "bollocks", "prick", "shithead", "fuckhead",
            "asshat", "douchebag", "scumbag", "jackass", "dipshit",
            // French
            "merde", "putain", "salope", "connard", "connasse", "enculé",
            "fils de pute", "va te faire foutre", "con", "conne", "crétin",
            "imbécile", "idiot", "abruti", "nique", "bâtard", "bâtarde",
            "pute", "pédé", "tapette", "pd", "fdp", "tg", "ntm",
            "bordel", "foutre", "merdique", "salopard", "saloparde",
            // Arabic (transliterated)
            "kess", "kol khara", "ibn el sharmouta", "sharmouta", "ayir",
            "zebi", "zabi", "khawal", "ahbal", "magnoun", "7mar", "hmar",
            "kalb", "weld el kahba", "kahba", "barra", "niha", "tiz",
            "nik", "nikha", "yel3an", "la3na"
    );

    // Pre-compiled patterns for performance (word boundary, case-insensitive)
    private static final List<Pattern> PATTERNS = BAD_WORDS.stream()
            .map(w -> Pattern.compile("(?i)\\b" + Pattern.quote(w) + "\\b"))
            .collect(Collectors.toList());

    /**
     * Returns true if the text contains any bad word.
     */
    public boolean containsBadWords(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        return PATTERNS.stream().anyMatch(p -> p.matcher(lower).find());
    }

    /**
     * Replaces each bad word occurrence with *** (preserving word length).
     */
    public String maskBadWords(String text) {
        if (text == null || text.isBlank()) return text;
        String result = text;
        for (Pattern p : PATTERNS) {
            java.util.regex.Matcher m = p.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "***");
            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }
}
