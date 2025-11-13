package org.sangyu.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.sangyu.shortlink.admin.common.convention.result.Result;
import org.sangyu.shortlink.admin.common.convention.result.Results;
import org.sangyu.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.sangyu.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import org.sangyu.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam 请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组列表
     * @return result
     */
    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup(){
        return Results.success(groupService.listGroup());
    }
}
