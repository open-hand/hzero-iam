package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpRoleAssignController测试类
 *
 * @author bo.he02@hand-china.com 2020/03/31 16:01
 */
public class SecGrpRoleAssignControllerTest extends BaseControllerTest {
    @Override
    protected String pathTemplate() {
        // organizationId
        return "/v1/%s/sec-grp-role-assign%s";
    }

    @Test
    public void test_001_whenRoleAssignSecGrpSuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        this.roleAssignSecGrp(1282L, 236L, Collections.singletonList(13119L));
    }

    @Test
    public void test_002_whenSecGrpRecycleRoleSuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        this.secGrpRecycleRole(1282L, 262L, Arrays.asList(13118L, 13119L));
    }

    /**
     * 给角色分配安全组
     *
     * @param organizationId 租户ID
     * @param roleId         角色ID
     * @param secGrpIds      分配的安全组
     */
    private void roleAssignSecGrp(Long organizationId, Long roleId, List<Long> secGrpIds) {
        try {
            this.roleAssignSecGrp(organizationId, roleId, this.objectMapper.writeValueAsString(secGrpIds));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 给角色分配安全组
     *
     * @param organizationId 租户ID
     * @param roleId         角色ID
     * @param requestContent 请求数据体
     */
    private void roleAssignSecGrp(Long organizationId, Long roleId, String requestContent) {
        try {
            // 发送请求
            this.mockMvc.perform(this.post(organizationId, String.format("/%s/assign-role", roleId))
                    .content(requestContent))
                    .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 安全组取消分配角色
     *
     * @param organizationId 租户ID
     * @param secGrpId       安全组ID
     * @param roleIds        角色IDs
     */
    private void secGrpRecycleRole(Long organizationId, Long secGrpId, List<Long> roleIds) {
        // 发送请求
        try {
            this.mockMvc.perform(this.delete(organizationId, String.format("/%s/recycle-role", secGrpId))
                    .content(this.objectMapper.writeValueAsString(roleIds)))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
