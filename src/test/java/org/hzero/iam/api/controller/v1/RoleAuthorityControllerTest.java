package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.databind.JavaType;
import io.choerodon.core.domain.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.iam.api.dto.RoleAuthorityDTO;
import org.hzero.iam.domain.entity.RoleAuthorityLine;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RoleAuthorityController测试类
 *
 * @author bo.he02@hand-china.com 2020/03/24 16:11
 */
public class RoleAuthorityControllerTest extends BaseControllerTest {
    /**
     * 租户ID
     */
    private static final Long ORGANIZATION_ID = 0L;
    /**
     * 角色ID
     */
    private static final Long ROLE_ID = 13100L;

    @Override
    protected String pathTemplate() {
        return "/v1/" + ORGANIZATION_ID + "/roles/" + ROLE_ID + "/role-auths%s";
    }

    @Before
    public void before() {
        // 模拟登录
        this.login("16304", 1L, Arrays.asList(1L, 13100L), Arrays.asList(2L, 13045L));
    }

    @Test
    public void test_001_WhenListSuccess() throws Exception {
        // 查询数据
        Page<RoleAuthorityDTO> roleAuthorityDTOS = this.list();

        if (CollectionUtils.isNotEmpty(roleAuthorityDTOS)) {
            roleAuthorityDTOS.forEach(roleAuthorityDTO -> {
                List<RoleAuthorityLine> roleAuthorityLines = roleAuthorityDTO.getRoleAuthorityLines();
                if (CollectionUtils.isNotEmpty(roleAuthorityLines)) {
                    roleAuthorityLines.forEach(roleAuthorityLine -> {
                        assert roleAuthorityDTO.getAuthScopeCode().equals(roleAuthorityLine.getDimensionType());
                    });
                }
            });
        }
    }

    /**
     * 选择所有
     */
    @Test
    public void test_002_WhenCheckAllSuccess() throws Exception {
        // 查询角色数据权限定义列表
        Page<RoleAuthorityDTO> roleAuthorityDTOS = this.list();
        // 设置选择
        this.setEnabledFlag(roleAuthorityDTOS, 1L);

        // 提交数据
        this.batchCreateOrUpdateRoleAuthority(roleAuthorityDTOS);

        // 查询角色数据权限定义列表
        roleAuthorityDTOS = this.list();
        // 断言结果
        this.assertEnabledFlag(roleAuthorityDTOS, 1L);
    }

    /**
     * 取消选择所有
     */
    @Test
    public void test_003_WhenUncheckAllSuccess() throws Exception {
        // 查询角色数据权限定义列表
        Page<RoleAuthorityDTO> roleAuthorityDTOS = this.list();
        // 设置选择
        this.setEnabledFlag(roleAuthorityDTOS, 0L);

        // 提交数据
        this.batchCreateOrUpdateRoleAuthority(roleAuthorityDTOS);

        // 查询角色数据权限定义列表
        roleAuthorityDTOS = this.list();
        // 断言结果
        this.assertEnabledFlag(roleAuthorityDTOS, 0L);
    }


    /**
     * 获取角色数据权限定义列表
     *
     * @return 满足条件的数据
     * @throws Exception 处理异常
     */
    private Page<RoleAuthorityDTO> list() throws Exception {
        // 查询角色授权数据
        String responseContent = this.mockMvc.perform(this.get("/" + ORGANIZATION_ID + "?page=0&size=100"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        // 响应结果类型
        JavaType responseType = this.objectMapper.getTypeFactory().constructParametricType(Page.class, RoleAuthorityDTO.class);
        // 响应结果数据
        return this.objectMapper.readValue(responseContent, responseType);
    }

    /**
     * 批量新增或保存角色数据权限定义明细
     *
     * @param roleAuthorityDTOS 角色数据权限定义数据
     * @throws Exception 处理异常
     */
    private void batchCreateOrUpdateRoleAuthority(Page<RoleAuthorityDTO> roleAuthorityDTOS) throws Exception {
        // 提交角色授权数据
        this.mockMvc.perform(this.post("")
                .content(this.objectMapper.writeValueAsString(roleAuthorityDTOS.getContent())))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    /**
     * 设置enabledFlag
     *
     * @param roleAuthorityDTOS 处理的角色权限数据
     * @param enabledFlag       是否启用 1 启用 0 未启用
     */
    private void setEnabledFlag(List<RoleAuthorityDTO> roleAuthorityDTOS, Long enabledFlag) {
        if (CollectionUtils.isNotEmpty(roleAuthorityDTOS)) {
            roleAuthorityDTOS.forEach(roleAuthorityDTO -> {
                roleAuthorityDTO.setDocEnabledFlag(enabledFlag);

                List<RoleAuthorityLine> roleAuthorityLines = roleAuthorityDTO.getRoleAuthorityLines();
                if (CollectionUtils.isNotEmpty(roleAuthorityLines)) {
                    roleAuthorityLines.forEach(roleAuthorityLine -> roleAuthorityLine.setEnabledFlag(enabledFlag));
                }
            });
        }
    }

    /**
     * 断言enabledFlag
     *
     * @param roleAuthorityDTOS 处理的角色权限数据
     * @param enabledFlag         是否启用 1 启用 0 未启用
     */
    private void assertEnabledFlag(List<RoleAuthorityDTO> roleAuthorityDTOS, Long enabledFlag) {
        if (CollectionUtils.isNotEmpty(roleAuthorityDTOS)) {
            roleAuthorityDTOS.forEach(roleAuthorityDTO -> {
                assert enabledFlag.equals(roleAuthorityDTO.getDocEnabledFlag());

                List<RoleAuthorityLine> roleAuthorityLines = roleAuthorityDTO.getRoleAuthorityLines();
                if (CollectionUtils.isNotEmpty(roleAuthorityLines)) {
                    roleAuthorityLines.forEach(roleAuthorityLine -> {
                        assert enabledFlag.equals(roleAuthorityLine.getEnabledFlag());
                    });
                }
            });
        }
    }
}
