package org.hzero.iam.api.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.hzero.iam.IamApplication
import org.hzero.iam.domain.entity.Menu
import org.hzero.iam.domain.entity.Permission
import org.hzero.iam.infra.constant.HiamMenuScope
import org.hzero.iam.util.UserLogin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.ResourceUtils
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
 * @author mingwei.liu@hand-china.com 2018/8/11
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("菜单导入API测试")
@Unroll
@Timeout(10)
class MenuControllerTest extends Specification {
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Shared
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(configurableApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    def "测试菜单导入API"() {
        when:
        MockMultipartFile menuFile = new MockMultipartFile("customMenuFiles",
                "menu.yaml",
                "text/vnd.yaml; charset=utf-8",
                ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "menu/menu.yaml").getBytes());
        MockMultipartFile menuZhFile = new MockMultipartFile("customMenuFiles",
                "language.zh.yaml",
                "text/vnd.yaml; charset=utf-8",
                ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "menu/language.zh.yaml").getBytes());
        MockMultipartFile menuEnFile = new MockMultipartFile("customMenuFiles",
                "language.en.yaml",
                "text/vnd.yaml; charset=utf-8",
                ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "menu/language.en.yaml").getBytes());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(menuImportUrl)
                .file(menuFile)
                .file(menuZhFile)
                .file(menuEnFile));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        where:
        menuImportUrl << ["/hzero/v1/menus/standard-menu-import", "/hzero/v1/1/menus/custom-menu-import"];
    }

    def "菜单展示-组织层菜单树形结构展示API"() {
        when:
        UserLogin.login("admin", 1, 0);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/menus/tree")
        .param("code", "choerodon.code.iam")
        .param("level", "organization"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    def "菜单管理-组织层菜单树形结构展示API"() {
        when:
        UserLogin.login("admin", 1, 0);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/menus/manage-tree")
                .param("scope", HiamMenuScope.BOTH)
                .param("code", "choerodon.code.iam")
                .param("level", "organization"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    def "组织层获取菜单列表API"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/${organizationId}/menus")
                .param("code", code)
                .param("withPermissions", "1"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        organizationId | code
        0              | "choerodon.code.iam"
        1              | "choerodon.code.custom.operation.iam"
    }

    def "组织层获取文件夹列表API"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/hzero/v1/${organizationId}/menus/dir")
                .param("level", level));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        organizationId | level
        1              | "site"
    }


    def "平台层更新菜单API"() {
        when:
        Menu menu = new Menu();
        menu.setId(id);
        menu.setDescription("Hell World========>mmmmnn");
        menu.setEnabledFlag(Boolean.FALSE);
        menu.setObjectVersionNumber(4);
        Permission permission = new Permission();
        permission.setCode("iam-service.organization.check");
        Permission permission2 = new Permission();
        permission2.setCode("iam-service.organization.disableOrganization");
        menu.setPermissions(Arrays.asList(permission, permission2));
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/menus/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menu)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        where:
        id << 97;
    }

    def "平台层创建菜单API"() {
        when:
        Menu menu = new Menu();
        menu.setCode("choerodon.code.iam.operation.helloworld-mmm");
        menu.setName("HelloWorld");
        menu.setLevel("site");
        menu.setParentId(43L);
        menu.setType("menu");
        menu.setSort(0);
        menu.setIcon("Hello");
        menu.setTenantId(0L);
        menu.setDescription("Hell World========>mmm");
        menu.setEnabledFlag(Boolean.TRUE);
        Permission permission = new Permission();
        permission.setCode("iam-service.organization.check");
        Permission permission2 = new Permission();
        permission2.setCode("iam-service.organization.disableOrganization");
        menu.setPermissions(Arrays.asList(permission, permission2));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/hzero/v1/menus/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menu)));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    def "平台层删除菜单API"() {
        when:
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/hzero/v1/menus/${menuId}"));

        then:
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();

        where:
        menuId << [43];
    }
}
