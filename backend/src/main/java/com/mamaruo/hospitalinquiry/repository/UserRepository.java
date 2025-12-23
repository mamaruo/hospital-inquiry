package com.mamaruo.hospitalinquiry.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamaruo.hospitalinquiry.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByMobile(String mobile);
    Optional<User> findByIdCard(String idCard);
}