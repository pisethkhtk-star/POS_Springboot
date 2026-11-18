package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.payload.pos.UserCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.UserUpdateRequest;

import java.util.List;

public interface PosUserService {
    List<User> getAllUsers();
    User createUser(UserCreateRequest request);
    User updateUser(Integer id, UserUpdateRequest request);
    void deleteUser(Integer id);
}
