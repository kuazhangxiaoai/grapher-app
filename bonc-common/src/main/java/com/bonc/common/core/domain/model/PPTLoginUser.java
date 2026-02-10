package com.bonc.common.core.domain.model;

import com.bonc.common.core.domain.model.PptUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class PPTLoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private PptUser user;

    private Collection<? extends GrantedAuthority> authorities;

    private Long loginTime;
    public Long getLoginTime()
    {
        return loginTime;
    }
    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    /**
     * 过期时间
     */
    private Long expireTime;
    public Long getExpireTime()
    {
        return expireTime;
    }
    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    /**
     * 用户唯一标识
     */
    private String token;
    public String getToken()
    {
        return token;
    }
    public void setToken(String token)
    {
        this.token = token;
    }

    public PPTLoginUser(PptUser user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public PptUser getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "0".equals(user.getStatus()); // 假设 status = "0" 表示启用
    }
}