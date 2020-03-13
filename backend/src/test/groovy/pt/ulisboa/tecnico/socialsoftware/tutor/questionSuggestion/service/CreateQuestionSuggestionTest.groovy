package pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.service

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
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.dto.QuestionSuggestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.QuestionSuggestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.QuestionSuggestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_NULL_ARGUMENTS
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_IS_TEACHER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_IN_COURSE

@DataJpaTest
class CreateQuestionSuggestionTest extends Specification {

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
    def course2
    def courseExecution
    def user1
    def user2
    def user3
    def questionDto1
    def questionDto2


    def setup() {

        course1 = new Course(COURSE_NAME1, Course.Type.TECNICO)
        courseRepository.save(course1)

        course2 = new Course(COURSE_NAME2, Course.Type.TECNICO)
        courseRepository.save(course2)

        courseExecution = new CourseExecution(course1, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user1 = new User('name1', "username1", 1, User.Role.STUDENT)
        user1.getCourseExecutions().add(courseExecution)
        user1.setEnrolledCoursesAcronyms(ACRONYM)
        courseExecution.getUsers().add(user1)
        userRepository.save(user1)
        user2 = new User('name2', "username2", 2, User.Role.STUDENT)
        user2.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user2)
        user2.setEnrolledCoursesAcronyms(ACRONYM)
        userRepository.save(user2)
        user3 = new User('name3', "username3", 3, User.Role.TEACHER)
        userRepository.save(user3)

        questionDto1 = new QuestionDto();
        questionDto1.setContent("content")
        questionDto1.setKey(1)
        questionDto1.setStatus(Question.Status.DISABLED.name())

        questionDto2 = new QuestionDto();
        questionDto2.setContent("content")
        questionDto2.setKey(2)
        questionDto2.setStatus(Question.Status.DISABLED.name())
    }

    def "create a question suggestion with no image and one option"() {
        given: "QuestionSuggestionDto"

        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE1)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionSuggestionDto.setOptions(options)

        when:
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course1.getId(), questionSuggestionDto)

        then: "the correct suggestion is inside the repository"

        def result = questionSuggestionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == QuestionSuggestion.Status.PENDING
        result.getTitle() == SUGGESTION_TITLE1
        result.getContent() == SUGGESTION_CONTENT
        result.getUser().getName() == 'name1'
        result.getImage() == null
        result.getOptions().size() == 1
        result.getUser().getQuestionsSuggestion().size() == 1
        result.getUser().getQuestionsSuggestion().contains(result)
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()
    }

    def "create a question with an image and two options"() {
        given: "SuggestionQuestionDto"
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
        questionSuggestionService.createSuggestionQuestion(user1.getId(),course1.getId(), questionSuggestionDto)

        then: "the correct suggestion is inside the repository"
        def result = questionSuggestionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == QuestionSuggestion.Status.PENDING
        result.getTitle() == SUGGESTION_TITLE
        result.getContent() == SUGGESTION_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == URL
        result.getImage().getWidth() == 20
        result.getOptions().size() == 2
    }

    def "create two suggestions for different students"() {
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionSuggestionDto.setOptions(options)


        def questionSuggestionDto2 = new QuestionSuggestionDto()
        questionSuggestionDto2.setQuestionDto(questionDto2)
        questionSuggestionDto2.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto2.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto2.setStatus(QuestionSuggestion.Status.PENDING.name())
        def optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_CONTENT)
        optionDto2.setCorrect(true)
        def options2 = new ArrayList<OptionDto>()
        options2.add(optionDto2)
        questionSuggestionDto2.setOptions(options2)

        when: 'are created two questions'
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course1.getId(), questionSuggestionDto)
        questionSuggestionService.createSuggestionQuestion(user2.getId(), course1.getId(), questionSuggestionDto2)

        then: "the two suggestions are created with the correct numbers"
        questionSuggestionRepository.count() == 2L
        def resultOne = questionSuggestionRepository.findAll().get(0)
        def resultTwo = questionSuggestionRepository.findAll().get(1)
        resultOne.getUser().getQuestionsSuggestion().size()==1
        resultOne.getUser().getQuestionsSuggestion().contains(resultOne)
        resultTwo.getUser().getQuestionsSuggestion().size()==1
        resultTwo.getUser().getQuestionsSuggestion().contains(resultTwo)
        resultOne.getUser().getName() == 'name1'
        resultTwo.getUser().getName() == 'name2'
    }


    def "create two suggestions for the same students"() {
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionSuggestionDto.setOptions(options)

        def questionSuggestionDto2 = new QuestionSuggestionDto()
        questionSuggestionDto2.setQuestionDto(questionDto2)
        questionSuggestionDto2.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto2.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto2.setStatus(QuestionSuggestion.Status.PENDING.name())
        def optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_CONTENT)
        optionDto2.setCorrect(true)
        def options2 = new ArrayList<OptionDto>()
        options2.add(optionDto2)
        questionSuggestionDto2.setOptions(options2)

        when: 'are created two questions'
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course1.getId(), questionSuggestionDto)
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course1.getId(), questionSuggestionDto2)

        then: "the two suggestions are created with the correct numbers"
        questionSuggestionRepository.count() == 2L
        def resultOne = questionSuggestionRepository.findAll().get(0)
        def resultTwo = questionSuggestionRepository.findAll().get(1)
        resultOne.getUser().getQuestionsSuggestion().size()==2
        resultOne.getUser().getQuestionsSuggestion().contains(resultOne)
        resultTwo.getUser().getQuestionsSuggestion().contains(resultOne)
        resultOne.getUser().getQuestionsSuggestion().contains(resultTwo)
        resultTwo.getUser().getQuestionsSuggestion().contains(resultTwo)
    }

    def "given user that doesnt exist"() {
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(42, course1.getId(), questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == USER_NOT_FOUND
    }

    def "given a professor"(){
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(user3.getId(), course1.getId(), questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == USER_IS_TEACHER
    }

    def "given null id"(){
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(null, course1.getId(), questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == INVALID_NULL_ARGUMENTS
    }


    def "given null suggestion dto"(){
        given: "nothing"

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course1.getId(), null)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == INVALID_NULL_ARGUMENTS
    }

    def "given null course"(){
        given: "SuggestionQuestionDto"

        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing course'
        questionSuggestionService.createSuggestionQuestion(user1.getId(), null, questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == INVALID_NULL_ARGUMENTS
    }

    def "given a course that a student isn't enrolled"(){
        given: "SuggestionQuestionDto"

        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setQuestionDto(questionDto1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing course'
        questionSuggestionService.createSuggestionQuestion(user1.getId(), course2.getId(), questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == USER_NOT_IN_COURSE
    }

    @TestConfiguration
    static class QuestionSuggestionServiceImplTestContextConfiguration {

        @Bean
        QuestionSuggestionService questionSuggestionService() {
            return new QuestionSuggestionService()
        }
    }
}
