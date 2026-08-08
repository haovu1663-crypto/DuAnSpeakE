package re.resultservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String audioId;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    // Pronunciation scores
    private Double pronunciationScore;
    private Double fluencyScore;
    private Double clarityScore;
    private Double accuracyScore;
    
    // Lưu danh sách lỗi dưới dạng JSON (đơn giản hóa)
    @Column(columnDefinition = "TEXT")
    private String mistakesJson;

    // AI Analysis
    @Column(columnDefinition = "TEXT")
    private String grammarCorrection;
    
    @Column(columnDefinition = "TEXT")
    private String grammarExplanation;
    
    private String vocabularyLevel;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
