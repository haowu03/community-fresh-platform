package com.fresh.modules.community.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.community.dto.CommentDTO;
import com.fresh.modules.community.dto.PostDTO;
import com.fresh.modules.community.dto.PostPageQueryDTO;
import com.fresh.modules.community.service.CommunityPostService;
import com.fresh.modules.community.vo.CommentVO;
import com.fresh.modules.community.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端-社区帖子
 */
@Slf4j
@RestController
@RequestMapping("/user/post")
@RequiredArgsConstructor
@Tag(name = "用户端-社区帖子")
public class UserPostController {

    private final CommunityPostService communityPostService;

    /**
     * 已通过帖子分页（按时间倒序）
     */
    @GetMapping("/page")
    @Operation(summary = "已通过帖子分页")
    public R<PageResult> page(PostPageQueryDTO dto) {
        return R.success(communityPostService.pageQuery(dto));
    }

    /**
     * 帖子详情（含评论列表）
     */
    @GetMapping("/{id}")
    @Operation(summary = "帖子详情")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        PostVO post = communityPostService.detail(id);
        List<CommentVO> comments = communityPostService.listComments(id);
        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("comments", comments);
        return R.success(result);
    }

    /**
     * 发帖（进入待审核）
     */
    @PostMapping
    @Operation(summary = "发帖")
    public R<Void> publish(@RequestBody PostDTO dto) {
        log.info("用户发帖: {}", dto);
        communityPostService.publish(dto);
        return R.success();
    }

    /**
     * 我的帖子（全部状态）
     */
    @GetMapping("/my")
    @Operation(summary = "我的帖子")
    public R<List<PostVO>> my() {
        return R.success(communityPostService.myPosts());
    }

    /**
     * 删除帖子（仅本人可删）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子")
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除帖子: {}", id);
        communityPostService.deleteById(id);
        return R.success();
    }

    /**
     * 评论
     */
    @PostMapping("/comment")
    @Operation(summary = "评论")
    public R<Void> comment(@RequestBody CommentDTO dto) {
        log.info("评论帖子: {}", dto);
        communityPostService.comment(dto);
        return R.success();
    }

    /**
     * 点赞/取消点赞（切换）
     */
    @PostMapping("/like/{id}")
    @Operation(summary = "点赞/取消点赞")
    public R<Void> like(@PathVariable Long id) {
        log.info("点赞切换: postId={}", id);
        communityPostService.likeToggle(id);
        return R.success();
    }
}
