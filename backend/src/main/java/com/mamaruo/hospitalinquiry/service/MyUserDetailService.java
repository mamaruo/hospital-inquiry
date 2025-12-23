package com.mamaruo.hospitalinquiry.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.UserPrincipal;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByMobile(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return new UserPrincipal(user);
    }
}