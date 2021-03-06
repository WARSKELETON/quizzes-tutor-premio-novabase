package pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.service.performanceTest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.QuestionSuggestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.QuestionSuggestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class AcceptQuestionSuggestionPerformanceTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String QUESTION_TITLE = "title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "option content"

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
    UserRepository userRepository

    def course = new Course()
    def user = new User("name", "username", 323, User.Role.STUDENT)
    def question = new Question()

    def setup() {
        userRepository.save(user)

        course.setName(COURSE_NAME)
        course.setType(Course.Type.TECNICO)
        courseRepository.save(course)

        def option = new Option()
        option.setContent(OPTION_CONTENT)
        option.setCorrect(true)
        option.setSequence(0)

        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        question.addOption(option)
        question.setType(Question.Type.SUGGESTION)
        question.setStatus(Question.Status.DISABLED)
        question.setCourse(course)
    }

    def "Performance testing to accept 10000 question suggestions"() {
        given: "10000 question suggestions"
        def questionSuggestions = new ArrayList<QuestionSuggestion>()
        for (def i = 0; i < 1; i++ ) {
            questionSuggestions[i] = new QuestionSuggestion()
            questionSuggestions[i].setUser(user)
            questionSuggestions[i].setQuestion(question)
            questionSuggestions[i].setStatus(QuestionSuggestion.Status.PENDING)
            questionSuggestionRepository.save(questionSuggestions[i])
        }

        when: "10000 suggestions are accepted"
        for (def i = 0; i < 1; i++ ) {
            questionSuggestionService.acceptQuestionSuggestion(questionSuggestions[i].getId())
        }

        then: true
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {

    }
}
