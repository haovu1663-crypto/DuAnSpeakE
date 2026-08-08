package re.pronunciationservice.component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tải và tra cứu CMU Pronouncing Dictionary (cmudict.dict).
 *
 * Format của từ điển:
 *   WORD  PH1 PH2 PH3 ...
 *   ví dụ: english  IH1 NG G L IH0 SH
 *
 * Các ký hiệu ARPAbet (phoneme) từ CMU:
 *   - Nguyên âm có số (0=unstressed, 1=primary stress, 2=secondary stress)
 *   - Phụ âm không có số
 *
 * Được nạp vào bộ nhớ 1 lần khi khởi động (PostConstruct).
 */
@Slf4j
@Component
public class CmuDictionary {

    // Map: lowercase word -> list of ARPAbet phonemes
    private final Map<String, List<String>> dictionary = new HashMap<>(134000);

    @PostConstruct
    public void load() {
        log.info("Loading CMU Pronouncing Dictionary...");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("cmudict.dict").getInputStream(),
                        StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Bỏ qua comment và dòng trống
                if (line.isEmpty() || line.startsWith(";;;") || line.startsWith("##")) continue;

                // Split: "word  PH1 PH2 PH3"
                // Dùng khoảng trắng đầu tiên để tách từ khỏi phonemes
                int firstSpace = line.indexOf(' ');
                if (firstSpace < 0) continue;

                String word = line.substring(0, firstSpace).toLowerCase();
                // Bỏ các biến thể (word(2), word(3)...) — dùng từ gốc
                if (word.contains("(")) word = word.substring(0, word.indexOf('('));

                String[] phonemes = line.substring(firstSpace).trim().split("\\s+");
                dictionary.put(word, Arrays.asList(phonemes));
                count++;
            }
            log.info("CMU Dictionary loaded: {} entries.", count);
        } catch (Exception e) {
            log.error("Failed to load cmudict.dict: {}", e.getMessage(), e);
        }
    }

    /**
     * Tra cứu danh sách phoneme của một từ.
     *
     * @param word từ cần tra cứu (không phân biệt hoa thường)
     * @return Optional chứa List<String> phoneme, hoặc empty nếu từ không có trong từ điển
     */
    public Optional<List<String>> getPhonemes(String word) {
        return Optional.ofNullable(dictionary.get(word.toLowerCase().trim()));
    }

    /**
     * Kiểm tra từ có tồn tại trong từ điển không.
     */
    public boolean contains(String word) {
        return dictionary.containsKey(word.toLowerCase().trim());
    }

    /**
     * Trả về tổng số entry đã nạp.
     */
    public int size() {
        return dictionary.size();
    }
}
