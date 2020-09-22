package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.lang.NonNull;
import org.hzero.iam.domain.entity.SecGrp;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpController测试类
 *
 * @author bo.he02@hand-china.com 2020/04/01 11:10
 */
public class SecGrpControllerTest extends BaseControllerTest {
    /**
     * 父安全组ID
     */
    private static final Long PARENT_SEC_GRP_ID = 228L;
    /**
     * 子安全组ID
     */
    private static final Long SON_SEC_GRP_ID = 229L;

    @Override
    protected String pathTemplate() {
        return "/v1/0/sec-grps%s";
    }

    @Test
    public void test_001_WhenDisabledParentSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询父安全组
        SecGrp parentSecGrp = this.query(PARENT_SEC_GRP_ID, 1L);

        // 禁用父安全组
        parentSecGrp.setEnabledFlag(0);

        // 更新父安全组
        this.update(parentSecGrp);
    }

    @Test
    public void test_002_WhenEnabledParentSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询父安全组
        SecGrp parentSecGrp = this.query(PARENT_SEC_GRP_ID, 1L);

        // 启用父安全组
        parentSecGrp.setEnabledFlag(1);

        // 更新父安全组
        this.update(parentSecGrp);
    }

    @Test
    public void test_003_WhenDisabledSonSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询子安全组
        SecGrp sonSecGrp = this.query(SON_SEC_GRP_ID, 13100L);

        // 禁用子安全组
        sonSecGrp.setEnabledFlag(0);

        // 更新子安全组
        this.update(sonSecGrp);
    }

    @Test
    public void test_004_WhenEnabledSonSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询子安全组
        SecGrp sonSecGrp = this.query(SON_SEC_GRP_ID, 13100L);

        // 启用子安全组
        sonSecGrp.setEnabledFlag(1);

        // 更新子安全组
        this.update(sonSecGrp);
    }

    @Test
    public void test_005_WhenDisabledSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询安全组
        SecGrp secGrp = this.query(237L, 2L);

        // 禁用安全组
        secGrp.setEnabledFlag(0);

        // 更新安全组
        this.update(secGrp);
    }

    @Test
    public void test_006_WhenEnabledSecGrpSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询安全组
        SecGrp secGrp = this.query(237L, 2L);

        // 启用安全组
        secGrp.setEnabledFlag(1);

        // 更新安全组
        this.update(secGrp);
    }

    @Test
    public void test_07_whenQuerySuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        // 查询安全组
        SecGrp secGrp = this.query(237L, 13116L);

        // 查询结果不能为空
        assert Objects.nonNull(secGrp);
    }

    @Test
    public void test_008_whenCreateSuccess() {
        // 模拟登录
        this.login_ZCCD();

        String requestContent = "{\"tenantId\":1284,\"enabledFlag\":1,\"secGrpCode\":\"ZC-CD-002\",\"secGrpName\":\"ZC-CD-002\",\"remark\":\"ZC-CD-002\",\"__id\":1054,\"_status\":\"create\",\"roleId\":13122}";
        // 创建安全组
        this.create(requestContent);
    }

    /**
     * 查询安全组
     *
     * @param secGrpId 安全组ID
     * @param roleId   角色ID
     * @return 查询结果安全组
     */
    private SecGrp query(@Nonnull Long secGrpId, @Nonnull Long roleId) {
        try {
            // 查询安全组
            String responseContent = this.mockMvc.perform(this.get(String.format("/%s?roleId=%s", secGrpId, roleId)))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString();

            // 返回结果
            return this.objectMapper.readValue(responseContent, SecGrp.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回空安全组
        return new SecGrp();
    }

    /**
     * 更新安全组
     *
     * @param secGrp 待更新的安全组
     */
    private void update(@Nonnull SecGrp secGrp) {
        try {
            // 更新数据
            this.mockMvc.perform(this.put("").content(this.objectMapper.writeValueAsString(secGrp)))
                    .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建安全组
     *
     * @param secGrp 安全组信息
     */
    private void create(@NonNull SecGrp secGrp) {
        try {
            this.create(this.objectMapper.writeValueAsString(secGrp));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建安全组
     *
     * @param requestContent 请求内容
     */
    private void create(@NonNull String requestContent) {
        try {
            this.mockMvc.perform(this.post("/create").content(requestContent))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
