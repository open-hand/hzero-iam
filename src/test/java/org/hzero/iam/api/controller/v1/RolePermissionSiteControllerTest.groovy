package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.domain.entity.RolePermission
import org.hzero.iam.domain.repository.RolePermissionRepository
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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Title
import spock.lang.Unroll

/**
 * <p>
 * 描述
 * </p>
 *
 * @author mingwei.liu@hand-china.com 2018/8/8
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("全局角色权限API测试")
@Unroll
@Timeout(10)
class RolePermissionSiteControllerTest extends Specification {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Autowired
    private RolePermissionRepository hiamRolePermissionRepository;
    @Shared
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "全局层更新角色权限"() {
        when:
        UserLogin.login("allen", 2, 1);
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(2);
        rolePermission.setPermissionId(1);
        rolePermission.setPermissionSetId(1);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/role-permissions/${roleId}/update")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(Arrays.asList(rolePermission))));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isNoContent()).andDo(MockMvcResultHandlers.print()).andReturn();

        where:
        roleId << [2];
    }
}
