package com.mamaruo.hospitalinquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamaruo.hospitalinquiry.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByMobile(String mobile);
}