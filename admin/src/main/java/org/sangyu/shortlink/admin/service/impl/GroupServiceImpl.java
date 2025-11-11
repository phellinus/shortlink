package org.sangyu.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.sangyu.shortlink.admin.dao.entity.GroupDO;
import org.sangyu.shortlink.admin.dao.mapper.GroupMapper;
import org.sangyu.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 短链接分组
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

}
