package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.databind.JavaType;
import io.choerodon.core.domain.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.iam.domain.vo.RoleVO;
import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpRoleAssignSiteController测试类
 *
 * @author bo.he02@hand-china.com 2020/03/31 16:01
 */
public class SecGrpRoleAssignSiteControllerTest extends BaseControllerTest {
    @Override
    protected String pathTemplate() {
        return "/v1/sec-grp-role-assign%s";
    }

    @Test
    public void test_001_whenListSecGrpAssignableRoleSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询数据
        Page<RoleVO> roleVOS = this.listSecGrpAssignableRole(237L, 1282L, "上海汉得-成都-何博");

        assert CollectionUtils.isNotEmpty(roleVOS);
    }

    /**
     * 查询安全组可以分配的角色
     *
     * @param secGrpId   安全组
     * @param tenantId   租户ID
     * @param tenantName 租户名称
     * @return 安全组可以分配的角色
     */
    private Page<RoleVO> listSecGrpAssignableRole(Long secGrpId, Long tenantId, String tenantName) {
        try {
            // 获取响应数据
            String responseContent = this.mockMvc.perform(this.get(String
                    .format("/%s/assignable-role?page=0&size=10&tenantId=%s&tenantName=%s",
                            secGrpId, tenantId, tenantName)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // 结果类型
            JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(Page.class, RoleVO.class);
            // 解析并返回结果
            return this.objectMapper.readValue(responseContent, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
