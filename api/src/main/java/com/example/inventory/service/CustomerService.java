package com.example.inventory.service;

import com.example.inventory.entity.User;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final UserRepository users;

    public CustomerService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User create(String email, String fullName) {
        return users.save(new User(email, fullName));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }
}
