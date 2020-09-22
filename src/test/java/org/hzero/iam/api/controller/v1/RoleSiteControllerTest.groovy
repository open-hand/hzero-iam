package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.domain.vo.RoleVO
import org.hzero.iam.util.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.*
/**
 * <p>
 * 描述
 * </p>
 *
 * @author mingwei.liu@hand-china.com 2018/8/6
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("平台层角色API测试")
@Unroll
@Timeout(10)
public class RoleSiteControllerTest extends Specification {
    private static String INHERITED_ROLE_CODE = "role/site/default/administrator";
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Shared
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "平台层查询角色详情API测试"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/roles/${roleId}"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        RoleVO roleVO = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVO.getInheritedRole() != null;
        and:
        roleVO.getInheritedRole().getCode() == INHERITED_ROLE_CODE;

        where:
        roleId << [3];
    }

    def "平台层查询角色详情API测试异常-不存在inheritRole情况"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/roles/${roleId}"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        RoleVO roleVO = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVO.getInheritedRole() == null;

        where:
        roleId << [1];
    }

    def "平台层分页查询权限集可用权限API测试"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/roles/${roleId}/permissions/paging")
        .param("permissionSetId", permissionSetId)
        .param("page", page)
        .param("size", size));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        roleId  | permissionSetId   | page | size
        38       | "4"               | "0"    | "1"
        38       | "4"               | "1"    | "1"
    }

    def "平台层全量查询权限集可用权限API测试"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/roles/${roleId}/permissions")
                .param("permissionSetId", permissionSetId));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        roleId  | permissionSetId
        38       | "4"
    }

    def "平台层全量查询权限集可用权限API测试-无Set"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/roles/${roleId}/permissions"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        roleId << [1, 38];
    }
}
