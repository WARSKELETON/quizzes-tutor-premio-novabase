package pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.service.performanceTest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.QuestionSuggestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.dto.QuestionSuggestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.QuestionSuggestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CreateQuestionSuggestionTestPerformanceTests extends Specification {

    public static final String COURSE_NAME1 = "Software Architecture"
    public static final String COURSE_NAME2 = "Operating Systems"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String SUGGESTION_TITLE = 'suggestion title'
    public static final String SUGGESTION_CONTENT = 'suggestion content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String SUGGESTION_TITLE1 = 'suggestion title1'

    @Autowired
    QuestionSuggestionService questionSuggestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuestionSuggestionRepository questionSuggestionRepository

    def course1
    def courseExecution
    def user1
    def questionDto1

    def setup() {

        course1 = new Course(COURSE_NAME1, Course.Type.TECNICO)
        courseRepository.save(course1)

        courseExecution = new CourseExecution(course1, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user1 = new User('name1', "username1", 1, User.Role.STUDENT)
        user1.getCourseExecutions().add(courseExecution)
        user1.setEnrolledCoursesAcronyms(ACRONYM)
        courseExecution.getUsers().add(user1)
        userRepository.save(user1)

        questionDto1 = new QuestionDto();
        questionDto1.setContent("content")
        questionDto1.setKey(1)
        questionDto1.setStatus(Question.Status.DISABLED.name())

    }

    def "performance testing to create 100000 Question Suggestion"(){
        given: "a question suggestion"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        and: 'an image'
        def image = new ImageDto()
        image.setUrl(URL)
        image.setWidth(20)
        questionSuggestionDto.setImage(image)

        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionSuggestionDto.setOptions(options)

        when:

        1.upto(100000, {questionSuggestionService.createSuggestionQuestion(user1.getId(),course1.getId(), questionSuggestionDto)})

        then: true

    }

    @TestConfiguration
    static class QuestionSuggestionServiceImplTestContextConfiguration {

        @Bean
        QuestionSuggestionService questionSuggestionService() {
            return new QuestionSuggestionService()
        }
    }
}
