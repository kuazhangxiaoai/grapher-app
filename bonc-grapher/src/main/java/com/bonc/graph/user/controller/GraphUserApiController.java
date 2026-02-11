package com.bonc.graph.user.controller;

import com.bonc.common.core.controller.BaseController;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.framework.web.service.TokenService;
import com.bonc.graph.user.domain.Result;
import com.bonc.graph.user.service.IGraphUserApiService;
import com.bonc.graph.utils.ObjToMapConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;

/**
 * graph用户信息Controller
 * 
 * @author wanghao
 * @date 2026-02-10
 */
@Slf4j
@RestController
@RequestMapping("/graph_api/v1/user")
public class GraphUserApiController extends BaseController
{
    @Autowired
    private IGraphUserApiService graphUserApiService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestParam("userName")String userName, @RequestParam("password")String passWord,
                        HttpServletRequest request)
    {
        log.info("开始调用登录接口:/graph_api/v1/user/login");
        log.info("userName:" + userName + ",passWord:" + passWord);
        Result result = new Result();
        GraphUser graphUser = graphUserApiService.login(userName, passWord);
        if (userName.isEmpty() || passWord.isEmpty() || graphUser == null){
            result.setResult("0002","账号或密码错误");
        }else {
            PPTLoginUser loginUser = new PPTLoginUser(graphUser, Collections.emptyList());
            // 生成token
            String token = tokenService.createPPTUserToken(loginUser);
            HashMap userMap = (HashMap) ObjToMapConvert.objToMap(graphUser);
            userMap.put("token", token);

            result.successResult(userMap);
        }
//        log.info("登录返回值：{}",result);

        return result;
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody GraphUser graphUser)
    {
        log.info("开始调用注册接口:/graph_api/v1/user/register");
        log.info("graphUser:"+ graphUser.toString());
        Result result = new Result();
        try {
            graphUserApiService.register(graphUser);
            result.successResult();
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result logout(HttpSession session)
    {
        log.info("开始调用登出接口:/graph_api/v1/user/logout");
        Result result = new Result();
        session.invalidate();
        result.successResult("用户已登出");
        return result;
    }

    /**
     * 返回用户信息
     */
    @GetMapping("/getUserInfo")
    public Result getUserInfo(@RequestParam("token") String token)
    {
        Result result = new Result();
        try {
            String userName = tokenService.getUsernameFromToken(token);
            result.successResult(userName);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
