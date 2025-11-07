package org.sangyu.shortlink.admin.common.enums;

import org.sangyu.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("B000200","用户记录不存在"),
    USER_NAME_EXIT("B000201","用户名已存在"),
    USER_EXIT("B000202","用户已存在"),
    USER_SAVE_ERROR("B000203","新增失败");
    private final String code;
    private final String message;


    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
