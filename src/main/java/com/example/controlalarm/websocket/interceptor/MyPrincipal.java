package com.example.controlalarm.websocket.interceptor;

import java.security.Principal;

/**
 * 自定义{@link Principal}
 *
 * @author linjiankai
 */
public class MyPrincipal implements Principal {
    private final String loginName;
    private final String groupId;

    public MyPrincipal(String loginName,String groupId) {
        this.loginName = loginName;
        this.groupId = groupId;
    }

    @Override
    public String getName() {
        return loginName;
    }
    public String getGroupId() {
        return groupId;
    }
}
