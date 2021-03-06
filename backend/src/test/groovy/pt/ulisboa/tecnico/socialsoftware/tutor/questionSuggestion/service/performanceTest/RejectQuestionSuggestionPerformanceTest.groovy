package pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.service.performanceTest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.QuestionSuggestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.dto.JustificationDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.JustificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.QuestionSuggestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class RejectQuestionSuggestionPerformanceTest extends Specification {

    public static final String QUESTION_TITLE = "question_title"
    public static final String QUESTION_CONTENT = "question_content"
    public static final String JUSTIFICATION_CONTENT = "justification_content"
    public static final String IMAGE_URL = "image_url"

    @Autowired
    QuestionSuggestionService questionSuggestionService

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    QuestionSuggestionRepository questionSuggestionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    JustificationRepository justificationRepository

    @Autowired
    UserRepository userRepository

    def question = new Question()

    def setup() {
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        question.setType(Question.Type.SUGGESTION)
        question.setStatus(Question.Status.DISABLED)
        questionRepository.save(question)
    }

    def "Performance testing to reject 10000 question suggestions"() {
        given: "a user"
        def user = new User()
        user.setKey(2)
        user.setRole(User.Role.TEACHER)
        userRepository.save(user)

        and: "an image"
        def imageDto = new ImageDto()
        imageDto.setUrl(IMAGE_URL)
        imageDto.setWidth(20)

        and: "a question suggestion"
        def questionSuggestions = new ArrayList<QuestionSuggestion>()

        and: "10000 justifications and suggestions"
        def justificationDto = new ArrayList<JustificationDto>()
        for (def i = 0; i < 1; i++) {
            justificationDto[i] = new JustificationDto()
            justificationDto[i].setKey(i)
            justificationDto[i].setContent(JUSTIFICATION_CONTENT)
            justificationDto[i].setImage(imageDto)

            questionSuggestions[i] = new QuestionSuggestion()
            questionSuggestions[i].setUser(user)
            questionSuggestions[i].setQuestion(question)
            questionSuggestions[i].setStatus(QuestionSuggestion.Status.PENDING)
            questionSuggestionRepository.save(questionSuggestions[i])
        }

        when: "10000 suggestions are rejected"
        for (def i = 0; i < 1; i++) {
            questionSuggestionService.rejectQuestionSuggestion(user.getId(), questionSuggestions[i].getId(), justificationDto[i])
        }

        then: true
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {

    }
}
