package org.sangyu.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sangyu.shortlink.admin.dao.entity.GroupDO;
import org.sangyu.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.sangyu.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    /**
     * 保存分组
     * @param groupName 分组名称
     */
   void saveGroup(String groupName);

    /**
     * 查询分组列表
     * @return result
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 更新短链接分组名称
     * @param requestParam 请求参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);
}
