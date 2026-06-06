package com.interview.api.controller;

import com.interview.api.dto.request.LoginRequest;
import com.interview.api.dto.request.RegisterRequest;
import com.interview.api.dto.response.LoginVO;
import com.interview.api.dto.response.UserVO;
import com.interview.common.result.Result;
import com.interview.common.util.JwtUtil;
import com.interview.dao.entity.User;
import com.interview.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 * 提供用户注册和登录功能，登录成功返回JWT Token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", request.getUsername()));
        if (existingUser != null) {
            return Result.error(1002, "用户已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(0);
        userMapper.insert(user);

        // 返回用户信息
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        return Result.success(vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", request.getUsername()));
        if (user == null) {
            return Result.error(1001, "用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(1003, "密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        return Result.success(vo);
    }
}
