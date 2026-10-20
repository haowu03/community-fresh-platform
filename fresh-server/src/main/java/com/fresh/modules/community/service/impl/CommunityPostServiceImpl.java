package com.fresh.modules.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.community.dto.CommentDTO;
import com.fresh.modules.community.dto.PostAuditDTO;
import com.fresh.modules.community.dto.PostDTO;
import com.fresh.modules.community.dto.PostPageQueryDTO;
import com.fresh.modules.community.entity.CommunityPost;
import com.fresh.modules.community.entity.PostComment;
import com.fresh.modules.community.entity.PostLike;
import com.fresh.modules.community.mapper.CommunityPostMapper;
import com.fresh.modules.community.mapper.PostCommentMapper;
import com.fresh.modules.community.mapper.PostLikeMapper;
import com.fresh.modules.community.service.CommunityPostService;
import com.fresh.modules.community.vo.CommentVO;
import com.fresh.modules.community.vo.PostVO;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区帖子 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost>
        implements CommunityPostService {

    /** 审核状态：待审核 */
    private static final Integer AUDIT_PENDING = 0;
    /** 审核状态：已通过 */
    private static final Integer AUDIT_APPROVED = 1;
    /** 审核状态：已拒绝 */
    private static final Integer AUDIT_REJECTED = 2;
    /** 评论状态：正常 */
    private static final Integer COMMENT_NORMAL = 1;

    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult pageQuery(PostPageQueryDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        Page<CommunityPost> page = new Page<>(dto.getPage(), dto.getPageSize());
        this.page(page, new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, AUDIT_APPROVED)
                .eq(dto.getType() != null, CommunityPost::getType, dto.getType())
                .orderByDesc(CommunityPost::getCreateTime));

        List<PostVO> voList = buildPostVOList(page.getRecords(), currentUserId);
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public PostVO detail(Long id) {
        CommunityPost post = this.getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        Long currentUserId = BaseContext.getCurrentId();
        // 未通过的帖子仅本人可见
        if (!AUDIT_APPROVED.equals(post.getStatus()) && !post.getUserId().equals(currentUserId)) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        return buildPostVO(post, currentUserId);
    }

    @Override
    public List<CommentVO> listComments(Long postId) {
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .eq(PostComment::getStatus, COMMENT_NORMAL)
                        .orderByAsc(PostComment::getCreateTime));
        if (CollUtil.isEmpty(comments)) {
            return Collections.emptyList();
        }
        // 批量查用户信息
        List<Long> userIds = comments.stream()
                .map(PostComment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CommentVO> all = comments.stream().map(c -> {
            CommentVO vo = BeanUtil.copyProperties(c, CommentVO.class);
            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setUserName(u.getName());
                vo.setAvatar(u.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        // 组装楼中楼：顶层评论挂载子评论
        Map<Long, List<CommentVO>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));
        List<CommentVO> topList = new ArrayList<>();
        for (CommentVO vo : all) {
            if (vo.getParentId() == null) {
                vo.setChildren(childrenMap.getOrDefault(vo.getId(), Collections.emptyList()));
                topList.add(vo);
            }
        }
        return topList;
    }

    @Override
    public void publish(PostDTO dto) {
        if (dto.getType() == null || StrUtil.isBlank(dto.getTitle())) {
            throw new BusinessException("帖子类型和标题不能为空");
        }
        CommunityPost post = BeanUtil.copyProperties(dto, CommunityPost.class);
        post.setUserId(BaseContext.getCurrentId());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(AUDIT_PENDING);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        this.save(post);
    }

    @Override
    public List<PostVO> myPosts() {
        Long currentUserId = BaseContext.getCurrentId();
        List<CommunityPost> list = this.list(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getUserId, currentUserId)
                .orderByDesc(CommunityPost::getCreateTime));
        return buildPostVOList(list, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CommunityPost post = this.getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        // 仅本人可删
        if (!post.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException("只能删除自己的帖子");
        }
        this.removeById(id);
        // 级联删除评论与点赞
        postCommentMapper.delete(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, id));
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comment(CommentDTO dto) {
        CommunityPost post = this.getById(dto.getPostId());
        if (post == null || !AUDIT_APPROVED.equals(post.getStatus())) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        if (StrUtil.isBlank(dto.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }
        PostComment comment = new PostComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(BaseContext.getCurrentId());
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setStatus(COMMENT_NORMAL);
        comment.setCreateTime(LocalDateTime.now());
        postCommentMapper.insert(comment);

        // 评论数+1
        post.setCommentCount(post.getCommentCount() + 1);
        this.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeToggle(Long id) {
        CommunityPost post = this.getById(id);
        if (post == null || !AUDIT_APPROVED.equals(post.getStatus())) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        Long currentUserId = BaseContext.getCurrentId();

        // 先查是否已赞
        PostLike exist = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, id)
                .eq(PostLike::getUserId, currentUserId));
        if (exist != null) {
            // 已赞 → 取消点赞
            postLikeMapper.deleteById(exist.getId());
            post.setLikeCount(Math.max(post.getLikeCount() - 1, 0));
        } else {
            // 未赞 → 插入；利用 post_like 唯一约束兜底并发重复插入
            try {
                PostLike like = new PostLike();
                like.setPostId(id);
                like.setUserId(currentUserId);
                like.setCreateTime(LocalDateTime.now());
                postLikeMapper.insert(like);
                post.setLikeCount(post.getLikeCount() + 1);
            } catch (DuplicateKeyException e) {
                // 并发下已存在点赞记录，视为已赞，不再重复计数
                log.warn("重复点赞被唯一约束拦截: postId={}, userId={}", id, currentUserId);
                return;
            }
        }
        this.updateById(post);
    }

    @Override
    public PageResult adminPage(Integer status, Integer page, Integer pageSize) {
        Page<CommunityPost> p = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : pageSize);
        this.page(p, new LambdaQueryWrapper<CommunityPost>()
                .eq(status != null, CommunityPost::getStatus, status)
                .orderByDesc(CommunityPost::getCreateTime));
        List<PostVO> voList = buildPostVOList(p.getRecords(), null);
        return new PageResult(p.getTotal(), voList);
    }

    @Override
    public void audit(PostAuditDTO dto) {
        if (dto.getStatus() == null
                || (!AUDIT_APPROVED.equals(dto.getStatus()) && !AUDIT_REJECTED.equals(dto.getStatus()))) {
            throw new BusinessException("审核结果不合法");
        }
        CommunityPost post = this.getById(dto.getId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        // 校验帖子当前为待审核
        if (!AUDIT_PENDING.equals(post.getStatus())) {
            throw new BusinessException("帖子已审核，请勿重复操作");
        }
        post.setStatus(dto.getStatus());
        post.setAuditRemark(dto.getAuditRemark());
        post.setAuditTime(LocalDateTime.now());
        post.setAuditorId(BaseContext.getCurrentId());
        post.setUpdateTime(LocalDateTime.now());
        this.updateById(post);
    }

    /**
     * 批量组装帖子VO（用户信息 + liked）
     */
    private List<PostVO> buildPostVOList(List<CommunityPost> posts, Long currentUserId) {
        if (CollUtil.isEmpty(posts)) {
            return Collections.emptyList();
        }
        List<Long> userIds = posts.stream()
                .map(CommunityPost::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查当前用户点赞记录
        Map<Long, Boolean> likedMap = Collections.emptyMap();
        if (currentUserId != null) {
            List<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
            likedMap = postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getUserId, currentUserId)
                            .in(PostLike::getPostId, postIds)).stream()
                    .collect(Collectors.toMap(PostLike::getPostId, l -> Boolean.TRUE));
        }

        Map<Long, Boolean> finalLikedMap = likedMap;
        return posts.stream().map(post -> {
            PostVO vo = BeanUtil.copyProperties(post, PostVO.class);
            User u = userMap.get(post.getUserId());
            if (u != null) {
                vo.setUserName(u.getName());
                vo.setAvatar(u.getAvatar());
            }
            vo.setLiked(finalLikedMap.getOrDefault(post.getId(), Boolean.FALSE));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 单个组装帖子VO
     */
    private PostVO buildPostVO(CommunityPost post, Long currentUserId) {
        return buildPostVOList(List.of(post), currentUserId).get(0);
    }
}
