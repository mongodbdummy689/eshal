package com.hajjumrah.repository;

import com.hajjumrah.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
    
    // Find regular users (non-admin) with pagination
    @Query("{'role': {$ne: 'ROLE_ADMIN'}}")
    Page<User> findRegularUsers(Pageable pageable);
    
    // Find regular users by name or email with pagination
    @Query("{'role': {$ne: 'ROLE_ADMIN'}, '$or': [{'fullName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}]}")
    Page<User> findRegularUsersBySearch(String searchTerm, Pageable pageable);
} 