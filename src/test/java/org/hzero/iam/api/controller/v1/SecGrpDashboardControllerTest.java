package org.hzero.iam.api.controller.v1;

import org.junit.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpDashboardController测试类
 *
 * @author bergturing 2020/04/09 16:22
 */
public class SecGrpDashboardControllerTest extends BaseControllerTest {

    @Override
    protected String pathTemplate() {
        return "/v1/0/sec-grp-acl-dashboards/%s";
    }

    @Test
    public void test_001_whenUpdateSuccess() throws Exception {
        // 模拟登录
        this.login_16304();

        String requestContent = "{\"objectVersionNumber\":7,\"_token\":\"keUs24wdEJx8Lgl9KbyHaW3Pyg8anglhvllGobP5iHWmKIqhst0BNdWtQk3dImWA5e+BneBIz/Q5mFdm/qTpjRmvjIX5r1vlr7gVQ55BDVt/OtrB4nVSLAi6AOZqiNmX\",\"secGrpAclDashboardId\":194,\"secGrpId\":237,\"tenantId\":0,\"cardId\":23,\"x\":100,\"y\":100,\"defaultDisplayFlag\":0,\"code\":\"HzeroMessage\",\"name\":\"系统消息/平台公告\",\"catalogType\":\"notice\",\"w\":50,\"h\":50,\"catalogMeaning\":\"通知类\"}";

        this.update(237L, requestContent);
    }

    /**
     * 租户层-安全组工作台维护-更新工作台配置列表
     *
     * @param secGrpId       安全组ID
     * @param requestContent 请求内容
     * @throws Exception 处理异常
     */
    private void update(Long secGrpId, String requestContent) throws Exception {
        this.mockMvc.perform(this.put(String.format("%s", secGrpId))
                .content(requestContent))
                .andExpect(status().is2xxSuccessful());
    }
}
