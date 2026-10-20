package com.fresh.modules.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知 Mapper
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
