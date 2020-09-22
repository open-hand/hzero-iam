package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.iam.domain.entity.Menu;
import org.junit.Test;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpAclController测试类
 *
 * @author bo.he02@hand-china.com 2020/04/13 17:25
 */
public class SecGrpAclControllerTest extends BaseControllerTest {
    @Override
    protected String pathTemplate() {
        return "/v1/%s/sec-grp-acls/%s%s";
    }

    @Test
    public void test_001_whenListAssignableAclSuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        // 查询数据
        List<Menu> menus = this.listAssignableAcl(1282L, 239L);

        assert CollectionUtils.isNotEmpty(menus);
    }

    /**
     * 查询可添加的权限集树
     *
     * @param organizationId 租户ID
     * @param secGrpId       安全组ID
     * @return 查询到的可添加的权限集树
     */
    private List<Menu> listAssignableAcl(Long organizationId, Long secGrpId) {
        try {
            String responseContent = this.mockMvc.perform(this.get(organizationId, secGrpId, String.format("?secGrpId=%s", secGrpId)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // 结果类型
            JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(List.class, Menu.class);

            // 转换结果,并返回结果
            return this.objectMapper.readValue(responseContent, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
