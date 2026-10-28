package com.medisetu.medisetu_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medisetu.medisetu_backend.entity.InterviewAnswer;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    List<InterviewAnswer> findBySessionId(Long sessionId);
    Optional<InterviewAnswer> findBySessionIdAndQuestionId(Long sessionId, String questionId);
}