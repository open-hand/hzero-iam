package org.hzero.iam.util;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import io.choerodon.core.oauth.CustomUserDetails;

import org.hzero.iam.domain.entity.User;

/**
 * 模拟用户登录工具
 *
 * @author bojiangzhou 2018/07/12
 */
public class UserLogin {

    /**
     * 模拟登陆
     *
     * @param loginName 登录名
     * @param userId 用户ID
     * @param organizationId 租户ID
     */
    public static void login(String loginName, Long userId, Long organizationId) {
        CustomUserDetails details = new CustomUserDetails(loginName, "", Collections.emptyList());
        details.setUserId(userId);
        details.setLanguage(User.DEFAULT_LANGUAGE);
        details.setTimeZone(User.DEFAULT_TIME_ZONE);
        details.setEmail("hand@hand-china.com");
        details.setOrganizationId(organizationId);

        AbstractAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(details, "", Collections.emptyList());
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
