package pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion;

@Repository
@Transactional
public interface QuestionSuggestionRepository extends JpaRepository <QuestionSuggestion, Integer>{
}