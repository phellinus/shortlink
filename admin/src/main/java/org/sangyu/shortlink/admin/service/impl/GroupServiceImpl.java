package org.sangyu.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.sangyu.shortlink.admin.dao.entity.GroupDO;
import org.sangyu.shortlink.admin.dao.mapper.GroupMapper;
import org.sangyu.shortlink.admin.service.GroupService;
import org.sangyu.shortlink.admin.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

/**
 * 短链接分组
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
           gid = RandomGenerator.generate();
        } while (!hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(RandomGenerator.generate())
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> wrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                //TODO 从上下文获取用户名
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(wrapper);
        return hasGroupFlag == null;
    }
}
