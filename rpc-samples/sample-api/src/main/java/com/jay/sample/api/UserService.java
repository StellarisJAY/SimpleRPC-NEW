package com.jay.sample.api;

import com.jay.sample.api.dto.UserDTO;

public interface UserService {
    UserDTO getUser(String name);
}
