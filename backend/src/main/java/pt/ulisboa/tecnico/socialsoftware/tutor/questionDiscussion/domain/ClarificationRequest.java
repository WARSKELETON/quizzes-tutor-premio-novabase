package pt.ulisboa.tecnico.socialsoftware.tutor.questionDiscussion.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

@Entity
@Table(name = "clarification_requests")
public class ClarificationRequest {
    public enum Status {
        CLOSED, OPEN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "question_answer_id")
    private QuestionAnswer questionAnswer;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    public ClarificationRequest() {
    }

    public ClarificationRequest(QuestionAnswer questionAnswer, User user, String content) {
        this.questionAnswer = questionAnswer;
        this.user = user;
        questionAnswer.setClarificationRequest(this);
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public QuestionAnswer getQuestionAnswer() { return questionAnswer; }

    public void setQuestionAnswer(QuestionAnswer questionAnswer) { this.questionAnswer = questionAnswer; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
