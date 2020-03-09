package pt.ulisboa.tecnico.socialsoftware.tutor.questionSugest.service

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
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.dto.QuestionSuggestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.repository.QuestionSuggestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.QuestionSuggestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionSuggestion.domain.QuestionSuggestion
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_NULL_ARGUMENTS
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_IS_TEACHER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class CreateQuestionSuggestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
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

    def user1
    def user2
    def user3


    def setup() {


        user1 = new User('name1', "username1", 1, User.Role.STUDENT)
        userRepository.save(user1)
        user2 = new User('name2', "username2", 2, User.Role.STUDENT)
        userRepository.save(user2)
        user3 = new User('name3', "username3", 3, User.Role.TEACHER)
        userRepository.save(user3)
        System.out.println(user2.getId())
    }

    def "create a question suggestion with no image and one option"() {
        given: "QuestionSuggestionDto"

        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setKey(1)
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
        questionSuggestionService.createSuggestionQuestion(user1.getId(), questionSuggestionDto)

        then: "the correct suggestion is inside the repository"

        def result = questionSuggestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == QuestionSuggestion.Status.PENDING
        result.getTitle() == SUGGESTION_TITLE1
        result.getContent() == SUGGESTION_CONTENT
        result.getUser().getName() == 'name1'
        result.getImage() == null
        result.getOptions().size() == 1
        result.getUser().getQuestionsSuggestion().size()==1
        result.getUser().getQuestionsSuggestion().contains(result)
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()
    }

    def "create a question with an image and two options"() {
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setKey(1)
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
        questionSuggestionService.createSuggestionQuestion(user1.getId(), questionSuggestionDto)

        then: "the correct suggestion is inside the repository"
        def result = questionSuggestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
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
        questionSuggestionDto.setKey(1)
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
        questionSuggestionDto2.setKey(2)
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
        questionSuggestionService.createSuggestionQuestion(user1.getId(), questionSuggestionDto)
        questionSuggestionDto.setKey(null)
        questionSuggestionService.createSuggestionQuestion(user2.getId(), questionSuggestionDto2)

        then: "the two suggestions are created with the correct numbers"
        questionSuggestionRepository.count() == 2L
        def resultOne = questionSuggestionRepository.findAll().get(0)
        def resultTwo = questionSuggestionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3
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
        questionSuggestionDto.setKey(1)
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
        questionSuggestionDto2.setKey(2)
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
        questionSuggestionService.createSuggestionQuestion(user1.getId(), questionSuggestionDto)
        questionSuggestionService.createSuggestionQuestion(user1.getId(), questionSuggestionDto2)

        then: "the two suggestions are created with the correct numbers"
        questionSuggestionRepository.count() == 2L
        def resultOne = questionSuggestionRepository.findAll().get(0)
        def resultTwo = questionSuggestionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3
        resultOne.getUser().getQuestionsSuggestion().size()==2
        resultOne.getUser().getQuestionsSuggestion().contains(resultOne)
        resultTwo.getUser().getQuestionsSuggestion().contains(resultOne)
        resultOne.getUser().getQuestionsSuggestion().contains(resultTwo)
        resultTwo.getUser().getQuestionsSuggestion().contains(resultTwo)
    }

    def "given user that doesnt exist"() {
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setKey(1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(42, questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == USER_NOT_FOUND
    }

    def "given a professor"(){
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setKey(1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(user3.getId(), questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == USER_IS_TEACHER
    }

    def "given null id"(){
        given: "SuggestionQuestionDto"
        def questionSuggestionDto = new QuestionSuggestionDto()
        questionSuggestionDto.setKey(1)
        questionSuggestionDto.setTitle(SUGGESTION_TITLE)
        questionSuggestionDto.setContent(SUGGESTION_CONTENT)
        questionSuggestionDto.setStatus(QuestionSuggestion.Status.PENDING.name())

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(null, questionSuggestionDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == INVALID_NULL_ARGUMENTS
    }


    def "given null suggestion dto"(){
        given: "nothing"

        when: 'a suggestion with a none existing student'
        questionSuggestionService.createSuggestionQuestion(1, null)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == INVALID_NULL_ARGUMENTS
    }

    @TestConfiguration
    static class QuestionSuggestionServiceImplTestContextConfiguration {

        @Bean
        QuestionSuggestionService questionSuggestionService() {
            return new QuestionSuggestionService()
        }
    }
}
