package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlExport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.TopicsXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TopicService {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CourseService courseService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findTopicCourse(int topicId) {
        return topicRepository.findById(topicId)
                .map(Topic::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TopicDto> findTopics(int courseId) {

        System.out.println("\n" +
                "TopicService : findTopics\n" +
                " - courseId: " + courseId + "\n"
        );

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));
        return topicRepository.findTopics(course.getId()).stream().sorted(Comparator.comparing(Topic::getName)).map(TopicDto::new).collect(Collectors.toList());
    }


    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TopicDto createTopic(int courseId, TopicDto topicDto) {

        System.out.println("\n" +
                "TopicService : createTopic\n" +
                " - courseId: " + courseId + "\n" +
                " - topicDto: " + topicDto + "\n"
        );

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));

        if (topicRepository.findTopicByName(course.getId(), topicDto.getName()) != null) {
            throw new TutorException(DUPLICATE_TOPIC, topicDto.getName());
        }

        Topic topic = new Topic(course, topicDto);
        topicRepository.save(topic);
        return new TopicDto(topic);
    }


    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TopicDto updateTopic(Integer topicId, TopicDto topicDto) {

        System.out.println("\n" +
                "TopicService : updateTopic\n" +
                " - topicId: " + topicId + "\n" +
                " - topicDto: " + topicDto + "\n"
        );

        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));

        topic.setName(topicDto.getName());
        return new TopicDto(topic);
    }


    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeTopic(Integer topicId) {

        System.out.println("\n" +
                "TopicService : removeTopic\n" +
                " - topicId: " + topicId + "\n"
        );

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicId));

        topic.remove();
        topicRepository.delete(topic);
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String exportTopics() {

        System.out.println("\n" +
                "TopicService : exportTopics\n"
        );

        TopicsXmlExport xmlExport = new TopicsXmlExport();

        return xmlExport.export(topicRepository.findAll());
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void importTopics(String topicsXML) {

        System.out.println("\n" +
                "TopicService : importTopics\n" +
                " - topicsXML: " + topicsXML + "\n"
        );

        TopicsXmlImport xmlImporter = new TopicsXmlImport();

        xmlImporter.importTopics(topicsXML, this, questionService, courseRepository);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void resetDemoTopics() {
        this.topicRepository.findTopics(courseService.getDemoCourse().getCourseId()).stream().filter(topic -> topic.getId() > 125).forEach(topic ->
                this.topicRepository.delete(topic)
        );
    }
}

