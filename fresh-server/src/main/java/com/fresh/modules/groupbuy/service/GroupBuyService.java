package com.fresh.modules.groupbuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.groupbuy.dto.GroupBuyLaunchDTO;
import com.fresh.modules.groupbuy.dto.GroupBuyPageQueryDTO;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.vo.GroupBuyActivityVO;
import com.fresh.modules.groupbuy.vo.GroupBuyMemberVO;

import java.util.List;

/**
 * 社区团购 Service
 */
public interface GroupBuyService extends IService<GroupBuyActivity> {

    /**
     * 管理端分页查询（含进度）
     */
    PageResult pageQuery(GroupBuyPageQueryDTO dto);

    /**
     * 用户端：进行中团购列表
     */
    List<GroupBuyActivityVO> listOngoing();

    /**
     * 团购详情（含进度，酌情缓存Redis）
     */
    GroupBuyActivityVO detail(Long id);

    /**
     * 我参的团
     */
    List<GroupBuyActivityVO> myJoin();

    /**
     * 发起团购（仅团长，自提点须属于该团长）
     */
    void launch(GroupBuyLaunchDTO dto);

    /**
     * 我发起的团购（团长）
     */
    List<GroupBuyActivityVO> myLaunch();

    /**
     * 参团成员列表
     */
    List<GroupBuyMemberVO> listMembers(Long id);

    /**
     * 管理端手动截团：进行中 → 达标置已成团，否则走流团逻辑
     */
    void close(Long id);

    /**
     * 结算单个到期活动（成团/流团），供定时任务/截团调用（独立事务）
     */
    void settleOne(GroupBuyActivity activity);
}
