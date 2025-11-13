package org.sangyu.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.sangyu.shortlink.admin.common.convention.result.Result;
import org.sangyu.shortlink.admin.common.convention.result.Results;
import org.sangyu.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.sangyu.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.sangyu.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import org.sangyu.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

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
     *
     * @param requestParam 请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组列表
     *
     * @return result
     */
    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 更新短链接分组名称
     *
     * @param requestParam 请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/group/update")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     *
     * @param gid 分组ID
     * @return result
     */
    @DeleteMapping("/api/short-link/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
}
