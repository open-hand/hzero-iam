package org.hzero.iam.api.controller.v1;

import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ToolPermissionController测试类
 *
 * @author bergturing
 */
public class ToolPermissionControllerTest extends BaseControllerTest {

    @Override
    protected String pathTemplate() {
        return "/v1/tool/permission%s";
    }

    @Test
    public void whenRefreshSuccess() {
        // 模拟登录
        this.login_16304();

        try {
            this.mockMvc.perform(this.post(String.format("/fresh?serviceName=%s&metaVersion=%s&cleanPermission=%s",
                    "hzero-iam-16304", "1.3.0", true))).andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
