package com.medisetu.medisetu_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "interview_answer",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_session_question", columnNames = {"session_id", "question_id"})
    }
)
@Getter 
@Setter 
@NoArgsConstructor
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "module_name", nullable = false)
    private String moduleName;

    @Column(name = "selected_option_id")
    private String selectedOptionId;

    @Column(name = "answer_text", length = 1000)
    private String answerText;

    @Column(name = "raw_utterance", length = 1000)
    private String rawUtterance;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_source", nullable = false)
    private InputSource inputSource;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}