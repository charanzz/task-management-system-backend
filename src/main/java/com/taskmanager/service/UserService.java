package com.taskmanager.service;

import com.taskmanager.entity.User;

public interface UserService {

    User registerUser(User user);

    User login(String email, String password);

    User getUserByEmail(String email);
}
