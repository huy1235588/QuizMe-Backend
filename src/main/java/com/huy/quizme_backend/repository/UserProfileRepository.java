package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
