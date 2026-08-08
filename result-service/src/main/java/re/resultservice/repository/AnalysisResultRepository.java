package re.resultservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import re.resultservice.entity.AnalysisResult;

import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findByAudioId(String audioId);
}
