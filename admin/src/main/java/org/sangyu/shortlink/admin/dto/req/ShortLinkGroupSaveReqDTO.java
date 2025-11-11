package org.sangyu.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 保存短链接的分组
 */
@Data
public class ShortLinkGroupSaveReqDTO {
    /**
     * 短链接名称
     */
    private String name;
}
