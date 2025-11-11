package org.sangyu.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 刷新token请求参数
 */
@Data
public class UserRefreshTokenReqDTO {

    /**
     * 刷新token
     */
    private String refreshToken;
}
