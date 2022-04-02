package com.example.controlalarm.websocket.controller;

import com.example.controlalarm.api.CommonResult;
import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.dto.UserDTO;
import com.example.controlalarm.websocket.entity.User;
import com.example.controlalarm.websocket.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linjiankai
 */
@RestController
//@RequestMapping("/websocketAlarm")
@Slf4j
public class WebsocketController {

    //拉取指定监听路径的未读的WebSocket消息
    //通过@MessageMapping ： 服务端处理客户端发来的STOMP消息
    //@SendTo： 上面msg会被广播到”/topic/shipAlarm”这个订阅路径中，只要客户端订阅了这条路径，不管是哪个用户，都会接收到消息
    //@SendToUser： 只发送给当前的客户端  broadcast属性，表明是否针对该用户广播。就是当有同一个用户登录多个session时，是否都能收到。取值true/false.
    @MessageMapping("/pullUnreadMessage")
    @SendToUser(value = "/topic/shipAlarm", broadcast = false)
//    public CommonResult<List<Object>> send(@Validated @RequestBody UserDTO vo, BindingResult result) {
    public CommonResult<List<Object>> send(@RequestBody UserDTO dto) {
        //存储消息的Redis列表名
        String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + dto.getOrgCode() + ":" + dto.getUserId() + ":" + dto.getDestination();
        //从Redis中拉取所有未读消息
        List<Object> messageList = RedisService.rangeList(listKey, 0, -1);

        if (messageList != null && messageList.size() > 0) {
            //删除Redis中的这个未读消息列表
            RedisService.delete(listKey);
            //将数据添加到返回集，供前台页面展示
            return CommonResult.success(messageList);
        }
        return CommonResult.success(new ArrayList<>());
    }

    /**
     * 连接websocket前进行身份验证
     *
     * @author linjiankai
     */
    @PostMapping("/check")
    public CommonResult check(@Validated @RequestBody User user, BindingResult result, HttpServletRequest request) {
        //session中添加用户信息
        HttpSession session = request.getSession();
        session.setAttribute(Constants.SESSION_USER, user);
        return CommonResult.success("");
    }

}
