package org.sangyu.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组排序请求参数
 */
@Data
public class ShortLinkGroupSortReqDTO {
    /**
     * 短链接分组ID
     */
    private String gid;
    /**
     * 排序
     */
    private Integer sortOrder;
}
