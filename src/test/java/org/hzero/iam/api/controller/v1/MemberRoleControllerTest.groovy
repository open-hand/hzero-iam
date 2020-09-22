package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.hzero.iam.IamApplication
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
 * @author mingwei.liu@hand-china.com 2018/8/3
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("组织层成员角色API测试")
@Unroll
@Timeout(10)
public class MemberRoleControllerTest extends Specification {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Shared
    private MockMvc mockMvc;

    ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "组织层查询分配用户的角色"() {
        when:
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/${organizationId}/member-roles/${userId}"));

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
        result.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("role")));

        MvcResult mvcResult = result.andReturn();
        List<RoleVO> roleVOList = JsonUtils.checkAndParseList(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVOList != null;
        and:
        roleVOList.size() > 0;

        where:
        organizationId  | userId
        "1"             | "1"
        "1"             | "2"
    }

    def "组织层查询分配给当前登录用户的角色"() {
        when:
        UserLogin.login("allen", 2, 1);
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/${organizationId}/member-roles/self"));

        then:
        result.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
        result.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("role")));

        MvcResult mvcResult = result.andReturn();
        List<RoleVO> roleVOList = JsonUtils.checkAndParseList(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVOList != null
        and:
        roleVOList.size() > 0;

        where:
        organizationId << [1]
    }

    def "组织层查询当前登录用户默认角色"() {
        when:
        UserLogin.login("allen", 2, 1);
        def result = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/${organizationId}/member-roles/self/default-role"));

        then:
        MvcResult mvcResult = result.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        RoleVO roleVO = JsonUtils.checkAndParseObject(mvcResult.getResponse().getContentAsString(), objectMapper, RoleVO.class);

        roleVO != null

        where:
        organizationId << [1]
    }

    def "组织层批量分配用户及角色API测试"() {
        when:
        UserLogin.login("allen", 2, 1);
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(13L);
        memberRole.setMemberType("user");
        memberRole.setAssignLevel("organization");
        memberRole.setAssignLevelValue(1L);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/${organizationId}/member-roles/batch-assign")
        .param("isEdit", isEdit)
        .param("memberIds", memberIds)
        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Arrays.asList(memberRole)))
        );

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        List<MemberRole> memberRoleList = JsonUtils.checkAndParseList(mvcResult.getResponse().getContentAsString(), objectMapper, MemberRole.class);

        memberRoleList != null;
        and:
        memberRoleList.size() > 0;

        where:
        organizationId | isEdit     | memberIds
        1              | "false"    | "3"
    }

    def "组织层批量分配用户及角色API测试-跨租户分配"() {
        when:
        UserLogin.login("allen", 2, 1);
        MemberRole memberRole = new MemberRole();
        memberRole.setRoleId(13L);
        memberRole.setMemberType("user");
        memberRole.setAssignLevel("organization");
        memberRole.setAssignLevelValue(3L);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/${organizationId}/member-roles/batch-assign")
                .param("isEdit", isEdit)
                .param("memberIds", memberIds)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Arrays.asList(memberRole)))
        );

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn();
        List<MemberRole> memberRoleList = JsonUtils.checkAndParseList(mvcResult.getResponse().getContentAsString(), objectMapper, MemberRole.class);

        memberRoleList != null;
        and:
        memberRoleList.size() > 0;

        where:
        organizationId | isEdit     | memberIds
        1              | "false"    | "4"
    }
}
