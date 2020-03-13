package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;

public enum ErrorMessage {
    QUIZ_NOT_FOUND("Quiz not found with id %d"),
    QUIZ_QUESTION_NOT_FOUND("Quiz question not found with id %d"),
    QUIZ_ANSWER_NOT_FOUND("Quiz answer not found with id %d"),
    QUESTION_ANSWER_NOT_FOUND("Question answer not found with id %d"),
    OPTION_NOT_FOUND("Option not found with id %d"),
    QUESTION_NOT_FOUND("Question not found with id %d"),
    USER_NOT_FOUND("User not found with id %d"),
    USER_NOT_FOUND_USERNAME("User not found with id %s"),
    TOPIC_NOT_FOUND("Topic not found with id %d"),
    ASSESSMENT_NOT_FOUND("Assessment not found with id %d"),
    TOPIC_CONJUNCTION_NOT_FOUND("Topic Conjunction not found with id %d"),
    COURSE_EXECUTION_NOT_FOUND("Course execution not found with name %d"),
    CLARIFICATION_REQUEST_NOT_FOUND("Question answer not found with id %d"),

    COURSE_NOT_FOUND("Course not found with name %s"),
    COURSE_NAME_IS_EMPTY("The course name is empty"),
    COURSE_TYPE_NOT_DEFINED("The course type is not defined"),
    CLARIFICATION_REQUEST_NOT_DEFINED("Clarification request is not defined"),
    CLARIFICATION_REQUEST_ANSWER_TYPE_NOT_DEFINED("Clarification request answer type is not defined"),
    COURSE_EXECUTION_ACRONYM_IS_EMPTY("The course execution acronym is empty"),
    COURSE_EXECUTION_ACADEMIC_TERM_IS_EMPTY("The course execution academic term is empty"),
    USERNAME_NOT_FOUND("Username %s not found"),

    QUIZ_USER_MISMATCH("Quiz %s is not assigned to student %s"),
    QUIZ_MISMATCH("Quiz Answer Quiz %d does not match Quiz Question Quiz %d"),
    QUESTION_OPTION_MISMATCH("Question %d does not have option %d"),
    COURSE_EXECUTION_MISMATCH("Course Execution %d does not have quiz %d"),

    DUPLICATE_TOPIC("Duplicate topic: %s"),
    DUPLICATE_USER("Duplicate user: %s"),
    DUPLICATE_COURSE_EXECUTION("Duplicate course execution: %s"),

    USERS_IMPORT_ERROR("Error importing users: %s"),
    QUESTIONS_IMPORT_ERROR("Error importing questions: %s"),
    TOPICS_IMPORT_ERROR("Error importing topics: %s"),
    ANSWERS_IMPORT_ERROR("Error importing answers: %s"),
    QUIZZES_IMPORT_ERROR("Error importing quizzes: %s"),

    QUESTION_IS_USED_IN_QUIZ("Question is used in quiz %s"),
    QUIZ_NOT_CONSISTENT("Field %s of quiz is not consistent"),
    USER_NOT_ENROLLED("%s - Not enrolled in any available course"),
    QUIZ_NO_LONGER_AVAILABLE("This quiz is no longer available"),
    QUIZ_NOT_YET_AVAILABLE("This quiz is not yet available"),
    CLARIFICATION_REQUEST_NO_LONGER_AVAILABLE("This clarification request is no longer available"),

    NO_CORRECT_OPTION("Question does not have a correct option"),
    NOT_ENOUGH_QUESTIONS("Not enough questions to create a quiz"),
    QUESTION_MISSING_DATA("Missing information for quiz"),
    QUESTION_MULTIPLE_CORRECT_OPTIONS("Questions can only have 1 correct option"),
    QUESTION_CHANGE_CORRECT_OPTION_HAS_ANSWERS("Can not change correct option of answered question"),
    QUIZ_HAS_ANSWERS("Quiz already has answers"),
    QUIZ_ALREADY_COMPLETED("Quiz already completed"),
    QUIZ_QUESTION_HAS_ANSWERS("Quiz question has answers"),
    FENIX_ERROR("Fenix Error"),
    AUTHENTICATION_ERROR("Authentication Error"),
    FENIX_CONFIGURATION_ERROR("Incorrect server configuration files for fenix"),

    QUESTION_ANSWER_MISMATCH_USER("Question answer %s is not assigned to student %s"),
    QUESTION_ANSWER_MISMATCH_QUESTION("Question answer %s is not assigned to question %s"),
    QUESTION_ANSWER_NOT_DEFINED("Question answer is not defined"),
    CLARIFICATION_REQUEST_IS_EMPTY("The clarification request content is empty"),
    CLARIFICATION_REQUEST_ANSWER_CONTENT_IS_EMPTY("The clarification request answer content must be defined"),

    ACCESS_DENIED("You do not have permission to view this resource"),
    CANNOT_OPEN_FILE("Cannot open file"),

    USER_IS_TEACHER("This operation is invalid to users of the type teacher"),
    USER_IS_STUDENT("This operation is invalid to users of the type student"),
    INVALID_NULL_ARGUMENTS_SUGGESTION("The question suggestion dto given is null"),
    INVALID_NULL_ARGUMENTS_SUGGESTIONID("The question suggestion Id given is null"),
    INVALID_NULL_ARGUMENTS_USERID("The User id given is null"),
    INVALID_NULL_ARGUMENTS_COUSEID("The Course id given given is null"),
    INVALID_NULL_ARGUMENTS_JUTIFICATIONDTO("The justification dto given given is null"),
    USER_NOT_IN_COURSE("The student is not enrolled in the given course"),

    QUESTION_SUGGESTION_ALREADY_ACCEPTED("This suggested question was already accepted"),
    QUESTION_SUGGESTION_ALREADY_REJECTED("This suggested question was already rejected"),
    QUESTION_SUGGESTION_NOT_FOUND("Question suggestion not found with id %d"),
    JUSTIFICATION_MISSING_DATA("Missing information for justification");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}