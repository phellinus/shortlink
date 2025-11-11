package org.sangyu.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.sangyu.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
}
