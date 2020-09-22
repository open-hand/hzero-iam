package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.domain.entity.Role
import org.hzero.iam.domain.entity.RolePermission
import org.hzero.iam.domain.entity.RolePermissionSet
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
 * @author mingwei.liu@hand-china.com 2018/8/6
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("租户层角色API测试")
@Unroll
@Timeout(10)
public class RoleControllerTest extends Specification {
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

    def "租户层创建角色API测试"() {
        when:
        UserLogin.login("allen", 2, 1);
        Role role = new Role();
        role.setCode(code);
        role.setLevel(level);
        role.setName(name);
        role.setParentRoleId(parentRoleId);
        RolePermissionSet rolePermissionSet = new RolePermissionSet();

        /**
         * #1
         */
        rolePermissionSet.setPermissionSetId(3);
        RolePermission rolePermission = new RolePermission(null, 1, 3);
        RolePermission rolePermission2 = new RolePermission(null, 2, 3);
        rolePermissionSet.setRolePermissions(Arrays.asList(rolePermission, rolePermission2));


        role.setRolePermissionSets(Arrays.asList(rolePermissionSet));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/${organizationId}/roles")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(role)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        Role resultRole = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, Role.class);

        resultRole != null;
        and:
        resultRole.getId() != null;

        where:
        organizationId  | code                           | level          | name      | parentRoleId
        1               | "role/organization/custom/abc2" | "organization" | "abc2管理员"| 2
        1               | "role/organization/custom/abc1" | "organization" | "abc1管理员"| 2
    }

    def "租户层更新角色API测试"() {
        when:
        UserLogin.login("allen", 2, 1);
        Role role = new Role();
        role.setId(38);
        role.setCode("role/organization/custom/[2.organization.0]/xxx");
        role.setLevel("organization");
        role.setName("XXX管理员");
        role.setParentRoleId(2);
        role.setTenantId(1);
        role.setObjectVersionNumber(10);
        RolePermissionSet rolePermissionSet = new RolePermissionSet();

        /**
         * #1
         */
        rolePermissionSet.setPermissionSetId(3);

        /**
         * #2
         */
        RolePermissionSet rolePermissionSet2 = new RolePermissionSet();
        RolePermission rolePermission = new RolePermission(null, 1, 5);
        RolePermission rolePermission2 = new RolePermission(null, 2, 5);
        rolePermissionSet2.setRolePermissions(Arrays.asList(rolePermission, rolePermission2));

        role.setRolePermissionSets(Arrays.asList(rolePermissionSet, rolePermissionSet2));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/hzero/v1/${organizationId}/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        Role resultRole = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, Role.class);

        resultRole != null;
        and:
        resultRole.getId() != null;

        where:
        organizationId << [1];
    }

    def "租户层创建角色API测试-跨租户"() {
        when:
        UserLogin.login("tom", 4, 1); // 切换了租户后的: tom本身是租户3, 但是拥有的角色13是租户1的
        Role role = new Role();
        role.setCode("role/organization/custom/kkk");
        role.setLevel("organization");
        role.setName("KKK管理员");
        role.setParentRoleId(13);
        RolePermission rolePermission = new RolePermission(null, 2, 1);
        role.setPermissionSets(Arrays.asList(rolePermission));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/${organizationId}/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        Role resultRole = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, Role.class);

        resultRole != null;
        and:
        resultRole.getId() != null;

        where:
        organizationId << [1];
    }

    def "租户层继承并创建角色API测试"() {
        when:
        UserLogin.login("allen", 2, 1);
        Role role = new Role();
        role.setCode("role/organization/custom/yyy");
        role.setLevel("organization");
        role.setName("YYY管理员");
        role.setParentRoleId(2);
        role.setInheritRoleId(11);
        RolePermission rolePermission = new RolePermission(null, 2, 2);
        role.setPermissionSets(Arrays.asList(rolePermission));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/${organizationId}/roles/inherit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        Role resultRole = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, Role.class);

        resultRole != null;
        and:
        resultRole.getId() != null;

        where:
        organizationId << [1];
    }
}
