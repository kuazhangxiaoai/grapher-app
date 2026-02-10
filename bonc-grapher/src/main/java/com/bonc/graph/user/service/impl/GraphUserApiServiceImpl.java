package com.bonc.graph.user.service.impl;

import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.md5.MD5Utils;
import com.bonc.graph.user.mapper.GraphUserApiMapper;
import com.bonc.graph.user.mapper.GraphUserMapper;
import com.bonc.graph.user.service.IGraphUserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * graph用户信息Service业务层处理
 *
 * @author wanghao
 * @date 2026-02-10
 */
@Service
public class GraphUserApiServiceImpl implements IGraphUserApiService {
    @Autowired
    private GraphUserApiMapper graphUserApiMapper;
    @Autowired
    private GraphUserMapper graphUserMapper;
    @Value("${md5salt}")
    private String md5salt;

    @Override
    public GraphUser login(String userName, String passWord) {
        String password = MD5Utils.md5WithSalt(passWord, md5salt);
        GraphUser graphUser = graphUserApiMapper.login(userName, password);

        if (graphUser != null) {
            graphUser.setPassword(null);
            graphUser.setDelFlag(null);
        }
        return graphUser;
    }

    public void register(GraphUser graphUser) {
        List<GraphUser> graphUsers = graphUserMapper.selectGraphUserList(new GraphUser());
        for (GraphUser user : graphUsers) {
            if (graphUser.getEmail() == null || "".equals(graphUser.getEmail())) {
                throw new RuntimeException("邮箱为空");
            }
            if (graphUser.getUserName() == null || "".equals(graphUser.getUserName())) {
                throw new RuntimeException("用户名为空");
            }
            if (graphUser.getPassword() == null || "".equals(graphUser.getPassword())) {
                throw new RuntimeException("密码为空");
            }
            if (graphUser.getNickName() == null || "".equals(graphUser.getNickName())) {
                throw new RuntimeException("昵称为空");
            }
            // 验证邮箱是否已存在
            if (graphUser.getEmail().equals(user.getEmail())) {
                throw new RuntimeException("该邮箱已被注册");
            }

            // 验证用户名是否已存在
            if (graphUser.getUserName().equals(user.getUserName())) {
                throw new RuntimeException("用户名已存在");
            }
        }

        String password = MD5Utils.md5WithSalt(graphUser.getPassword(), md5salt);
        // 创建用户
        GraphUser newGraphUser = new GraphUser();
        newGraphUser.setEmail(graphUser.getEmail());
        newGraphUser.setUserName(graphUser.getUserName());
        newGraphUser.setPassword(password);
        newGraphUser.setNickName(graphUser.getNickName());
        newGraphUser.setStatus("0");
        newGraphUser.setSex("2");
        newGraphUser.setDelFlag("0");
        newGraphUser.setCreateTime(DateUtils.getNowDate());

        graphUserMapper.insertGraphUser(newGraphUser);
    }
}
