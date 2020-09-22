package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.api.dto.MemberRoleAssignDTO
import org.hzero.iam.domain.entity.MemberRole
import org.hzero.iam.domain.vo.RoleVO
import org.hzero.iam.util.UserLogin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
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
 * @author mingwei.liu@hand-china.com 2018/8/4
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("全局层成员角色API测试")
@Unroll
@Timeout(10)
public class MemberRoleSiteControllerTest extends Specification {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Shared
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "查询管理员用户所创建的角色子树"() {
        when:
        UserLogin.login("admin", 1, 1);
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/member-roles/create-roles-subtree/${parentRoleId}")
        .param("roleSource", "custom"));

        then:
        MvcResult mvcResult = result.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        RoleVO roleVO = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVO != null;

        where:
        parentRoleId << [2];
    }

    def "全局层批量分配成员角色至租户"() {
        when:
        UserLogin.login("allen", 2, 1);
        MemberRoleAssignDTO memberRoleAssignOnTenantLevelDTO =
                new MemberRoleAssignDTO(null, "tom", null, "role/organization/custom/yyy-(2.organization.1)");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/member-roles/assign-to-tenant-level")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(Arrays.asList(memberRoleAssignOnTenantLevelDTO))));

        then:
        MvcResult mvcResult = resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("\$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].roleId").value(13L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].memberId").value(4L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].memberType").value("user"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].sourceId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].sourceType").value("organization"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].assignLevel").value("organization"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].assignLevelValue").value(3L))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    def "全局层批量分配成员角色(按照成员角色列表维度)"() {
        when:
        UserLogin.login("allen", 2, 1);
        MemberRole memberRole =
                new MemberRole(null, 38L, 4L, "user", null, null, "organization", 3L);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/member-roles/batch-assign/by-member-role-list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(memberRole))));

        then:
        MvcResult mvcResult = resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("\$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].roleId").value(38L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].memberId").value(4L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].memberType").value("user"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].sourceId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].sourceType").value("organization"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].assignLevel").value("organization"))
                .andExpect(MockMvcResultMatchers.jsonPath("\$[0].assignLevelValue").value(3L))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }
}
