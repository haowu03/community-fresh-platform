package com.fresh.modules.notice.controller;

import com.fresh.common.result.R;
import com.fresh.modules.notice.entity.Notice;
import com.fresh.modules.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端-消息通知接口
 */
@Slf4j
@RestController
@RequestMapping("/user/notice")
@RequiredArgsConstructor
@Tag(name = "消息通知模块", description = "用户端-消息通知接口")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 查询当前用户通知列表（倒序）
     */
    @GetMapping("/list")
    @Operation(summary = "消息通知列表")
    public R<List<Notice>> list() {
        return R.success(noticeService.listByUser());
    }

    /**
     * 标记通知已读
     */
    @PutMapping("/read/{id}")
    @Operation(summary = "标记已读")
    public R<Void> read(@PathVariable Long id) {
        noticeService.markRead(id);
        return R.success();
    }
}
