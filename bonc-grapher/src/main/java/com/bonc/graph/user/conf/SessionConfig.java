package com.bonc.graph.user.conf;

import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SessionConfig {
    public static Map<String, HttpSession> sessions = new HashMap<>();
}
