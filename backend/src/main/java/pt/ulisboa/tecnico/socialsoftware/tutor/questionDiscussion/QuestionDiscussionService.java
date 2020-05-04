package pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.domain.ClarificationRequestAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.domain.PublicClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.dto.PublicClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.repository.ClarificationRequestAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.repository.ClarificationRequestRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class QuestionDiscussionService {

    @Autowired
    QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository;

    @Autowired
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
        value = { SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto createClarificationRequest(Integer questionAnswerId, ClarificationRequestDto clarificationRequestDto) {
        QuestionAnswer questionAnswer = getQuestionAnswer(questionAnswerId);

        Question question = getQuestion(clarificationRequestDto);

        User user = getUser(clarificationRequestDto.getUsername());

        checkQuestionAnswerMatchUser(questionAnswer, user);

        checkQuestionAnswerMatchQuestion(questionAnswer, question);

        ClarificationRequest clarificationRequest = new ClarificationRequest(questionAnswer, question, user, clarificationRequestDto.getContent());
        if (clarificationRequestDto.getImage() != null) {
            addImage(clarificationRequestDto, clarificationRequest);
        }

        entityManager.persist(clarificationRequest);
        return new ClarificationRequestDto(clarificationRequest);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationRequestDto> getClarificationRequests(String username, Integer executionId) {
        User user = getUser(username);
        Set<ClarificationRequest> clarificationRequests = new HashSet<>();

        if (user.getRole() == User.Role.STUDENT) {
            clarificationRequests = user.getClarificationRequests();
        }

        if (user.getRole() == User.Role.TEACHER) {
            clarificationRequests = clarificationRequestRepository.findClarificationRequestsByCourseExecutions(executionId);
        }

        return clarificationRequests.stream()
                .filter(clarificationRequest -> clarificationRequest.getQuestionAnswer().getQuizQuestion().getQuiz().getCourseExecution().getId().equals(executionId))
                .map(ClarificationRequestDto::new)
                .sorted(Comparator.comparing(ClarificationRequestDto::getStatus))
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto getClarification(Integer questionAnswerId) {
        QuestionAnswer questionAnswer = getQuestionAnswer(questionAnswerId);

        return questionAnswer.getClarificationRequest().stream()
                .map(ClarificationRequestDto::new)
                .collect(Collectors.toList()).get(0);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PublicClarificationRequestDto createPublicClarificationRequest(ClarificationRequestDto clarificationRequestDto) {

        ClarificationRequest clarificationRequest = this.getClarificationRequest(clarificationRequestDto.getId());
        Course course = clarificationRequest.getQuestion().getCourse();

        PublicClarificationRequest publicClarificationRequest = new PublicClarificationRequest(clarificationRequest, course);

        clarificationRequest.setPublicClarificationRequest(publicClarificationRequest);
        course.addPublicClarificationRequests(publicClarificationRequest);

        return new PublicClarificationRequestDto(publicClarificationRequest);
    }

    private Question getQuestion(ClarificationRequestDto clarificationRequestDto) {
        return questionRepository
                    .findById(clarificationRequestDto.getQuestionAnswerDto().getQuestion().getId())
                    .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, clarificationRequestDto.getQuestionAnswerDto().getQuestion().getId()));
    }

    private QuestionAnswer getQuestionAnswer(Integer questionAnswerId) {
        if (questionAnswerId == null) {
            throw new TutorException(QUESTION_ANSWER_NOT_DEFINED);
        }
        return questionAnswerRepository.findById(questionAnswerId)
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswerId));
    }

    private User getUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new TutorException(USER_NOT_FOUND_USERNAME, username);
        }
        return user;
    }

    private void addImage(ClarificationRequestDto clarificationRequestDto, ClarificationRequest clarificationRequest) {
        Image img = new Image(clarificationRequestDto.getImage());
        clarificationRequest.setImage(img);
        img.setClarificationRequest(clarificationRequest);
    }

    private void checkQuestionAnswerMatchQuestion(QuestionAnswer questionAnswer, Question question) {
        if (question != questionAnswer.getQuizQuestion().getQuestion()) {
            throw new TutorException(QUESTION_ANSWER_MISMATCH_QUESTION, String.valueOf(questionAnswer.getId()), String.valueOf(question.getId()));
        }
    }

    private void checkQuestionAnswerMatchUser(QuestionAnswer questionAnswer, User user) {
        if (user != questionAnswer.getQuizAnswer().getUser()) {
            throw new TutorException(QUESTION_ANSWER_MISMATCH_USER, String.valueOf(questionAnswer.getId()), user.getUsername());
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestAnswerDto createClarificationRequestAnswer(Integer clarificationRequestId, ClarificationRequestAnswerDto clarificationRequestAnswerDto) {

        ClarificationRequest clarificationRequest = getClarificationRequest(clarificationRequestId);

        User user = getUser(clarificationRequestAnswerDto.getUsername());

        checkClarificationRequestAnswerType(clarificationRequestAnswerDto);

        ClarificationRequestAnswer clarificationRequestAnswer = new ClarificationRequestAnswer(clarificationRequest, clarificationRequestAnswerDto.getType(), user, clarificationRequestAnswerDto.getContent());

        entityManager.persist(clarificationRequestAnswer);
        return new ClarificationRequestAnswerDto(clarificationRequestAnswer);
    }

    private ClarificationRequest getClarificationRequest(Integer clarificationRequestId) {
        if (clarificationRequestId == null) {
            throw new TutorException(CLARIFICATION_REQUEST_NOT_DEFINED);
        }

        return clarificationRequestRepository.findById(clarificationRequestId)
                .orElseThrow(() -> new TutorException(CLARIFICATION_REQUEST_NOT_FOUND, clarificationRequestId));
    }

    private void checkClarificationRequestAnswerType(ClarificationRequestAnswerDto clarificationRequestAnswerDto) {
        if (clarificationRequestAnswerDto.getType() == null) {
            throw new TutorException(CLARIFICATION_REQUEST_ANSWER_TYPE_NOT_DEFINED);
        }
    }
}
