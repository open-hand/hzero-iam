package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.api.dto.AutoProcessResultDTO
import org.hzero.iam.api.dto.TenantAdminRoleAndDataPrivAutoAssignmentDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
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
@Title("成员角色外部辅助API测试")
@Unroll
@Timeout(10)
public class MemberRoleExternalControllerTest extends Specification {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Shared
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "成员角色外部辅助API-自动分配管理员角色和数据权限测试"() {
        when:
        TenantAdminRoleAndDataPrivAutoAssignmentDTO tenantAdminRoleAndDataPrivAutoAssignmentDTO =
                new TenantAdminRoleAndDataPrivAutoAssignmentDTO();
        tenantAdminRoleAndDataPrivAutoAssignmentDTO.setUserId(4L);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO.setTenantId(3L);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO.setNewTenantFlag(Boolean.TRUE);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO.setCompanyNum("XXX");

        TenantAdminRoleAndDataPrivAutoAssignmentDTO tenantAdminRoleAndDataPrivAutoAssignmentDTO2 =
                new TenantAdminRoleAndDataPrivAutoAssignmentDTO();
        tenantAdminRoleAndDataPrivAutoAssignmentDTO2.setUserId(99L);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO2.setTenantId(99L);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO2.setNewTenantFlag(Boolean.TRUE);
        tenantAdminRoleAndDataPrivAutoAssignmentDTO2.setCompanyNum("YYY");

        def result = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/member-roles-external/auto-assign")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(objectMapper.writeValueAsString(Arrays.asList(tenantAdminRoleAndDataPrivAutoAssignmentDTO,
        tenantAdminRoleAndDataPrivAutoAssignmentDTO2))));

        then:
        MvcResult mvcResult = result.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        List<AutoProcessResultDTO> autoProcessResultDTOList = JsonUtils.checkAndParseList(mvcResult.getResponse().getContentAsString(), objectMapper, AutoProcessResultDTO.class);

        autoProcessResultDTOList != null;
        and:
        autoProcessResultDTOList.size() == 2;
    }
}
