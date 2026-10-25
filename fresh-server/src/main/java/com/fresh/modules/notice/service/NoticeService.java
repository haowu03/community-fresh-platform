package com.fresh.modules.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.notice.entity.Notice;

import java.util.List;

/**
 * 消息通知 Service
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 发送站内通知（供订单/团购等其他模块调用）
     *
     * @param userId  接收用户id
     * @param type    类型 1自提通知 2成团通知 3流团退款通知 4系统通知
     * @param title   标题
     * @param content 内容
     */
    void send(Long userId, Integer type, String title, String content);

    /**
     * 查询当前登录用户的通知列表（按创建时间倒序）
     *
     * @return 通知列表
     */
    List<Notice> listByUser();

    /**
     * 标记通知为已读（仅允许操作本人通知）
     *
     * @param id 通知id
     */
    void markRead(Long id);
}
