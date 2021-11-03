package com.jay.sample.server.api;

import com.jay.rpc.annotation.RpcService;
import com.jay.sample.api.UserService;
import com.jay.sample.api.dto.UserDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO getUser(String name) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);
        userDTO.setUsername(name);
        userDTO.setPwd(name + "123");
        return userDTO;
    }
}
