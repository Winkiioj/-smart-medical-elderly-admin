package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知 Mapper — A 负责
 *
 * @author A
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /** 某用户未读消息数 */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    /** 全部标记已读 */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);
}
