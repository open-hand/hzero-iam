package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.oauth.CustomUserDetails;
import org.hzero.iam.BaseTest;
import org.hzero.iam.domain.repository.UserRepository;
import org.hzero.iam.infra.common.utils.UserUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 抽象的Controller测试基类
 *
 * @author bo.he02@hand-china.com 2020/02/27 11:01
 */
public abstract class BaseControllerTest extends BaseTest {
    /**
     * mvc模拟器
     */
    protected MockMvc mockMvc;
    /**
     * 类型转换器
     */
    @Autowired
    protected ObjectMapper objectMapper;
    /**
     * 用户信息资源库
     */
    @Autowired
    private UserRepository userRepository;
    /**
     * 应用上下文
     */
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    /**
     * 实际请求路径
     *
     * @param args 参数
     * @return 实际请求路径
     */
    protected String path(Object... args) {
        // 返回实际路径
        return String.format(this.pathTemplate(), args);
    }

    /**
     * post
     *
     * @param args 路径上的参数
     * @return 构建器
     */
    protected MockHttpServletRequestBuilder post(Object... args) {
        return MockMvcRequestBuilders.post(this.path(args)).contentType(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * get
     *
     * @param args 路径上的参数
     * @return 构建器
     */
    protected MockHttpServletRequestBuilder get(Object... args) {
        return MockMvcRequestBuilders.get(this.path(args)).contentType(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * put
     *
     * @param args 路径上的参数
     * @return 构建器
     */
    protected MockHttpServletRequestBuilder put(Object... args) {
        return MockMvcRequestBuilders.put(this.path(args)).contentType(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * delete
     *
     * @param args 路径上的参数
     * @return 构建器
     */
    protected MockHttpServletRequestBuilder delete(Object... args) {
        return MockMvcRequestBuilders.delete(this.path(args)).contentType(MediaType.APPLICATION_JSON_UTF8);
    }

    /**
     * 模拟登录
     *
     * @param loginName 登录用户名
     */
    protected void login(String loginName) {
        UserUtils.login(this.userRepository, loginName);
    }

    /**
     * 模拟登录
     *
     * @param loginName     登录用户名
     * @param siteRoleIds   平台级角色ID
     * @param tenantRoleIds 租户级角色ID
     * @@param currentRoleId 当前角色ID
     */
    protected void login(String loginName, Long currentRoleId, List<Long> siteRoleIds, List<Long> tenantRoleIds) {
        // 模拟登录
        this.login(loginName);

        // 获取用户详情
        CustomUserDetails userDetails = UserUtils.getUserDetails();

        userDetails.setRoleId(currentRoleId);
        // 设置平台级角色ID
        userDetails.setSiteRoleIds(siteRoleIds);
        // 设置租户级角色ID
        userDetails.setTenantRoleIds(tenantRoleIds);
    }

    /**
     * 用户【16304】模拟登录
     */
    protected void login_16304() {
        // 模拟登录
        this.login("16304", 1L, Arrays.asList(1L, 13100L), Arrays.asList(2L, 13045L));
    }

    /**
     * 用户【bergturing-16304】模拟登录
     */
    protected void login_bergturing_16304() {
        // 模拟登录
        this.login("bergturing-16304", 13116L, Collections.emptyList(), Arrays.asList(13116L, 13118L, 13119L));
    }

    /**
     * 用户【ZCCD】模拟登录
     */
    protected void login_ZCCD() {
        // 模拟登录
        this.login("ZCCD", 13122L, Collections.emptyList(), Arrays.asList(13122L, 13126L));
    }

    /**
     * 路径模板
     *
     * @return 路径模板
     */
    protected abstract String pathTemplate();
}
