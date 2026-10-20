package com.fresh.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.notice.entity.Notice;
import com.fresh.modules.notice.mapper.NoticeMapper;
import com.fresh.modules.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知 Service 实现类
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    /** 未读 */
    private static final Integer UNREAD = 0;
    /** 已读 */
    private static final Integer READ = 1;

    /**
     * 发送站内通知（供订单/团购等其他模块调用）
     */
    @Override
    public void send(Long userId, Integer type, String title, String content) {
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsRead(UNREAD);
        notice.setCreateTime(LocalDateTime.now());
        save(notice);
        log.info("发送站内通知: userId={}, type={}, title={}", userId, type, title);
    }

    /**
     * 查询当前登录用户的通知列表（按创建时间倒序）
     */
    @Override
    public List<Notice> listByUser() {
        Long userId = BaseContext.getCurrentId();
        return list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getUserId, userId)
                .orderByDesc(Notice::getCreateTime));
    }

    /**
     * 标记通知为已读：校验通知归属当前用户
     */
    @Override
    public void markRead(Long id) {
        Long userId = BaseContext.getCurrentId();
        Notice notice = getById(id);
        if (notice == null || !notice.getUserId().equals(userId)) {
            throw new BusinessException("通知不存在");
        }
        update(new LambdaUpdateWrapper<Notice>()
                .eq(Notice::getId, id)
                .set(Notice::getIsRead, READ));
    }
}
