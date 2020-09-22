package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.infra.common.utils.UserUtils
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
public class PermissionControllerTest extends Specification {
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
    def "分页查询用户权限API测试"() {
        when:
        UserUtils.login("admin", 1, 0)
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/permissions/self")
        .param("level", level));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        level  | page | size
        "site" | "0"    | "1"
        "site" | "1"    | "1"
    }
}
