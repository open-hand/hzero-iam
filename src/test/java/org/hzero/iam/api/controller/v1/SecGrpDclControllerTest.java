package org.hzero.iam.api.controller.v1;

import io.choerodon.mybatis.domain.AuditDomain;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hzero.iam.api.dto.SecGrpDclDTO;
import org.hzero.iam.domain.entity.SecGrpDclLine;
import org.hzero.iam.infra.constant.Constants;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpDclController测试类
 *
 * @author bo.he02@hand-china.com 2020/04/01 13:41
 */
public class SecGrpDclControllerTest extends BaseControllerTest {
    /**
     * 父安全组ID
     */
    private static final Long PARENT_SEC_GRP_ID = 228L;
    /**
     * 子安全组ID
     */
    private static final Long SON_SEC_GRP_ID = 229L;
    /**
     * 日志打印对象
     */
    private static Logger logger = LoggerFactory.getLogger(SecGrpDclControllerTest.class);

    @Override
    protected String pathTemplate() {
        return "/v1/0/sec-grp-dcls%s";
    }

    @Test
    public void test_001_whenQuerySecGrpDclAuthoritySuccess() {
        // 模拟登录
        this.login_16304();

        // 权限类型编码
        String authorityTypeCode = Constants.DocLocalAuthorityTypeCode.COMPANY;

        // 查询父安全组数据权限
        SecGrpDclDTO parentSecGrpDclDTO = this.querySecGrpDclAuthority(PARENT_SEC_GRP_ID, 1L, authorityTypeCode);
        logger.info(ReflectionToStringBuilder.toString(parentSecGrpDclDTO, ToStringStyle.MULTI_LINE_STYLE));

        logger.info("========================================================================================");

        // 查询子安全组数据权限
        SecGrpDclDTO sonSecGrpDclDTO = this.querySecGrpDclAuthority(SON_SEC_GRP_ID, 13100L, authorityTypeCode);
        logger.info(ReflectionToStringBuilder.toString(sonSecGrpDclDTO, ToStringStyle.MULTI_LINE_STYLE));
    }

    @Test
    public void test_002_whenDeleteSecGrpDclAuthoritySuccess() {
        // 模拟登录
        this.login_16304();

        // 安全组ID
        Long secGrpId = 237L;
        // 角色ID
        Long roleId = 2L;
        // 权限类型编码
        String authorityTypeCode = Constants.DocLocalAuthorityTypeCode.PURORG;

        // 查询数据
        SecGrpDclDTO secGrpDclDTO = this.querySecGrpDclAuthority(secGrpId, roleId, authorityTypeCode);

        // 获取行数据
        List<SecGrpDclLine> secGrpDclLineList = secGrpDclDTO.getSecGrpDclLineList();
        if (CollectionUtils.isNotEmpty(secGrpDclLineList)) {
            // 行数据不为空，删除第一条数据
            this.deleteSecGrpDclAuthority(secGrpId, authorityTypeCode, secGrpDclLineList.subList(0, 1));
        }
    }

    @Test
    public void test_003_whenQuerySecGrpDclAuthoritySuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        // 查询数据
        SecGrpDclDTO secGrpDclDTO = this.querySecGrpDclAuthority(236L, 13116L, Constants.DocLocalAuthorityTypeCode.COMPANY);

        logger.info("SecGrpDclDTO: {}", ReflectionToStringBuilder.toString(secGrpDclDTO, ToStringStyle.MULTI_LINE_STYLE));
    }

    @Test
    public void test_004_whenQuerySecGrpDclAssignableAuthoritySuccess() {
        // 模拟登录
        this.login_bergturing_16304();

        // 查询数据
        SecGrpDclDTO secGrpDclDTO = this.querySecGrpDclAssignableAuthority(236L, 13116L,
                Constants.DocLocalAuthorityTypeCode.PURORG);

        logger.info("SecGrpDclDTO: {}", ReflectionToStringBuilder.toString(secGrpDclDTO, ToStringStyle.MULTI_LINE_STYLE));
    }

    /**
     * 查询安全组数据权限
     *
     * @param secGrpId 安全组ID
     * @param roleId   角色ID
     * @return 查询结果
     */
    private SecGrpDclDTO querySecGrpDclAuthority(@Nonnull Long secGrpId,
                                                 @Nonnull Long roleId,
                                                 @Nonnull String authorityTypeCode) {
        try {
            // 获取响应结果
            String responseContent = this.mockMvc.perform(this.get(String
                    .format("/%s/authority?roleId=%s&authorityTypeCode=%s&secGrpSource=self",
                            secGrpId, roleId, authorityTypeCode)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // 解析并返回结果
            return this.objectMapper.readValue(responseContent, SecGrpDclDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 我的安全组查询可以添加的数据权限
     *
     * @param secGrpId 安全组ID
     * @param roleId   角色ID
     * @return 查询结果
     */
    private SecGrpDclDTO querySecGrpDclAssignableAuthority(@Nonnull Long secGrpId,
                                                           @Nonnull Long roleId,
                                                           @Nonnull String authorityTypeCode) {
        try {
            // 获取响应结果
            String responseContent = this.mockMvc.perform(this.get(String
                    .format("/%s/authority/assignable?roleId=%s&authorityTypeCode=%s&secGrpSource=self",
                            secGrpId, roleId, authorityTypeCode)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // 解析并返回结果
            return this.objectMapper.readValue(responseContent, SecGrpDclDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除安全组数据权限
     *
     * @param secGrpId          安全组ID
     * @param authorityTypeCode 权限类型编码
     * @param secGrpDclLines    安全组数据权限行列表
     */
    private void deleteSecGrpDclAuthority(@Nonnull Long secGrpId,
                                          @Nonnull String authorityTypeCode,
                                          @Nonnull List<SecGrpDclLine> secGrpDclLines) {
        try {
            // 设置状态为删除
            secGrpDclLines.forEach(secGrpDclLine -> secGrpDclLine.set_status(AuditDomain.RecordStatus.delete));
            // 请求
            this.deleteSecGrpDclAuthority(secGrpId, authorityTypeCode, this.objectMapper.writeValueAsString(secGrpDclLines));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除安全组数据权限
     *
     * @param secGrpId          安全组ID
     * @param authorityTypeCode 权限类型编码
     * @param requestContent    安全组数据权限行数据
     */
    private void deleteSecGrpDclAuthority(@Nonnull Long secGrpId,
                                          @Nonnull String authorityTypeCode,
                                          @Nonnull String requestContent) {
        try {
            // 发送请求
            this.mockMvc.perform(this.delete(String.format("/%s/authority?authorityTypeCode=%s",
                    secGrpId, authorityTypeCode)).content(requestContent))
                    .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
