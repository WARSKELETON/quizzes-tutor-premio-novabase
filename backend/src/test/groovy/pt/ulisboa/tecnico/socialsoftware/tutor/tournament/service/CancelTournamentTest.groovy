package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Specification
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NON_CREATOR
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_ENROLLING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class CancelTournamentTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String COURSE_ACRONYM = "SA1"
    public static final String ACADEMIC_TERM = "First Semester"
    public static final String CREATOR_NAME = "Creator Name"
    public static final String CREATOR_USERNAME = "Creator Username"
    public static final String ENROLLER_NAME = "Enroller Name"
    public static final String ENROLLER_USERNAME = "Enroller Username"
    public static final String TOURNAMENT_TITLE = "Tournament Title"
    public static final String AVAILABLE_DATE = "2020-01-25T16:30:11Z"
    public static final String CONCLUSION_DATE = "2020-01-25T17:40:11Z"

    @Autowired
    TournamentService tournamentService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository userRepository

    def course = new Course()
    def courseExecution = new CourseExecution()
    def creator = new User()
    def enroller = new User()
    def tournament = new Tournament()

    def setup() {
        course.setName(COURSE_NAME)
        course.setType(Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution.setCourse(course)
        courseExecution.setType(Course.Type.TECNICO)
        courseExecution.setAcronym(COURSE_ACRONYM)
        courseExecution.setAcademicTerm(ACADEMIC_TERM)
        courseExecutionRepository.save(courseExecution)

        creator.setName(CREATOR_NAME)
        creator.setUsername(CREATOR_USERNAME)
        creator.setKey(1)
        creator.setRole(User.Role.STUDENT)
        creator.addCourseExecutions(courseExecution)
        userRepository.save(creator)

        enroller.setName(ENROLLER_NAME)
        enroller.setUsername(ENROLLER_USERNAME)
        enroller.setKey(2)
        enroller.setRole(User.Role.STUDENT)
        enroller.addCourseExecutions(courseExecution)
        userRepository.save(enroller)

        tournament.setTitle(TOURNAMENT_TITLE)
        tournament.setCourseExecution(courseExecution)
        tournament.setCreator(creator)
        tournament.setAvailableDate(DateHandler.toLocalDateTime(AVAILABLE_DATE))
        tournament.setConclusionDate(DateHandler.toLocalDateTime(CONCLUSION_DATE))
        tournament.setStatus(Tournament.Status.ENROLLING)
        tournamentRepository.save(tournament)
    }

    def "The creator of a tournament cancels a created tournament"() {
        when: "the creator tries to cancel"
        tournamentService.cancelTournament(creator.getId(), tournament.getId())

        then: "the tournament becomes cancelled"
        tournament.getStatus() == Tournament.Status.CANCELLED
    }

    def "Cannot cancel a tournament given an invalid user id"() {
        when: "the creator tries to cancel"
        tournamentService.cancelTournament(0, tournament.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == USER_NOT_FOUND
    }

    def "Cannot cancel a tournament given an invalid tournament id"() {
        when: "the creator tries to cancel"
        tournamentService.enrollInTournament(creator.getId(), 0)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == TOURNAMENT_NOT_FOUND
    }

    def "Cannot cancel a tournament given an user that is not the tournament creator"() {
        when: "a student not creator tries to cancel"
        tournamentService.cancelTournament(enroller.getId(), tournament.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == TOURNAMENT_NON_CREATOR
    }

    @Unroll
    def "Cannot cancel a tournament that is #test"() {
        given: "a tournament that is #test"
        tournament.setStatus(status)

        when: "the creator tries to cancel"
        tournamentService.cancelTournament(creator.getId(), tournament.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        test        | status                      || errorMessage
        "ongoing"   | Tournament.Status.ONGOING   || TOURNAMENT_NOT_ENROLLING
        "concluded" | Tournament.Status.CONCLUDED || TOURNAMENT_NOT_ENROLLING
        "cancelled" | Tournament.Status.CANCELLED || TOURNAMENT_NOT_ENROLLING
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {

    }
}
