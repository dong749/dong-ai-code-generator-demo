package com.dong.dongaicodegenerator.controller;

import com.dong.dongaicodegenerator.common.BaseResponse;
import com.dong.dongaicodegenerator.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("hello");
    }
}
