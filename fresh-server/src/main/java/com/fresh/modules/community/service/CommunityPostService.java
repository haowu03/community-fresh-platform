package com.fresh.modules.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.community.dto.CommentDTO;
import com.fresh.modules.community.dto.PostAuditDTO;
import com.fresh.modules.community.dto.PostDTO;
import com.fresh.modules.community.dto.PostPageQueryDTO;
import com.fresh.modules.community.entity.CommunityPost;
import com.fresh.modules.community.vo.CommentVO;
import com.fresh.modules.community.vo.PostVO;

import java.util.List;

/**
 * 社区帖子 Service
 */
public interface CommunityPostService extends IService<CommunityPost> {

    /**
     * 用户端分页：只查已通过，按时间倒序，组装 liked
     */
    PageResult pageQuery(PostPageQueryDTO dto);

    /**
     * 帖子详情（含用户信息、liked）
     */
    PostVO detail(Long id);

    /**
     * 帖子评论列表（含用户信息，楼中楼）
     */
    List<CommentVO> listComments(Long postId);

    /**
     * 发帖（status=0 待审核）
     */
    void publish(PostDTO dto);

    /**
     * 我的帖子（全部状态）
     */
    List<PostVO> myPosts();

    /**
     * 删除帖子（仅本人可删）
     */
    void deleteById(Long id);

    /**
     * 评论（帖子评论数+1）
     */
    void comment(CommentDTO dto);

    /**
     * 点赞切换：已赞则取消，未赞则点赞
     */
    void likeToggle(Long id);

    /**
     * 管理端分页（按审核状态筛选）
     */
    PageResult adminPage(Integer status, Integer page, Integer pageSize);

    /**
     * 审核：通过/拒绝
     */
    void audit(PostAuditDTO dto);
}
