package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.databind.JavaType;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.domain.AuditDomain;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.iam.domain.entity.SecGrpAclField;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpFieldController测试类
 *
 * @author bo.he02@hand-china.com 2020/03/31 16:01
 */
public class SecGrpFieldControllerTest extends BaseControllerTest {
    /**
     * 父安全组ID
     */
    private static final Long PARENT_SEC_GRP_ID = 228L;
    /**
     * 子安全组ID
     */
    private static final Long SON_SEC_GRP_ID = 229L;
    /**
     * 权限ID
     */
    private static final Long PERMISSION_ID = 179690L;
    /**
     * 字段ID
     */
    private static final Long FILED_ID = 49L;
    /**
     * 字段类型含义
     */
    private static final String FIELD_TYPE_MEANING = "STRING";
    /**
     * 权限类型：隐藏
     */
    private static final String PERMISSION_TYPE_HIDE = "HIDE";
    /**
     * 权限类型：加密
     */
    private static final String PERMISSION_TYPE_ENCRYPTION = "ENCRYPTION";

    @Override
    protected String pathTemplate() {
        return "/v1/0/sec-grp-acl-fields%s";
    }

    @Test
    public void test_001_WhenListFiledSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询数据
        Page<SecGrpAclField> secGrpAclFields = this.listField(PARENT_SEC_GRP_ID);

        if (CollectionUtils.isNotEmpty(secGrpAclFields)) {
            secGrpAclFields.forEach(secGrpAclField -> {
                assert PARENT_SEC_GRP_ID.equals(secGrpAclField.getSecGrpId());
                assert PERMISSION_ID.equals(secGrpAclField.getPermissionId());
            });
        }
    }

    @Test
    public void test_002_WhenCreateUpdateDeleteFieldSuccess() {
        // 模拟登录
        this.login_16304();

        // 初始化
        this.init();


        // 创建
        this.createField(PARENT_SEC_GRP_ID);
        // 更新
        this.updateField(PARENT_SEC_GRP_ID);
        // 删除
        this.deleteField(PARENT_SEC_GRP_ID);
    }

    /**
     * 当父安全组新建字段之后，子安全组不可删除
     * 当父安全组删除之后，子安全组可删除
     */
    @Test
    public void test_003_WhenBatchSaveSuccess_001() {
        // 模拟登录
        this.login_16304();

        // 初始化
        this.init();

        // 父安全组创建
        this.createField(PARENT_SEC_GRP_ID);
        // 子安全组不可删除
        this.assertDeleteEnableFlag(this.listField(SON_SEC_GRP_ID), 0);

        // 父安全组删除
        this.deleteField(PARENT_SEC_GRP_ID);
        // 子安全组可删除
        this.assertDeleteEnableFlag(this.listField(SON_SEC_GRP_ID), 1);
    }

    /**
     * 当子安全组创建后，然后父安全组创建了，子安全组不可删除
     * 当父安全删除后，子安全组可删除
     */
    @Test
    public void test_004_WhenBatchSaveSuccess_002() {
        // 模拟登录
        this.login_16304();

        // 初始化
        this.init();

        // 子安全组创建
        this.createField(SON_SEC_GRP_ID);
        // 父安全组创建
        this.createField(PARENT_SEC_GRP_ID);
        // 子安全组不可删除
        this.assertDeleteEnableFlag(this.listField(SON_SEC_GRP_ID), 0);

        // 父安全组删除
        this.deleteField(PARENT_SEC_GRP_ID);
        // 子安全组可删除
        this.assertDeleteEnableFlag(this.listField(SON_SEC_GRP_ID), 1);
    }

    @Test
    public void test_005_whenCreateFieldSuccess() {
        // 模拟登录
        this.login_16304();

        this.createField(237L);
    }

    @Test
    public void test_006_whenDeleteFieldSuccess() {
        // 模拟登录
        this.login_16304();

        this.deleteField(237L);
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化父安全组
        // 删除所有字段
        this.deleteField(PARENT_SEC_GRP_ID);


        // 初始化子安全组
        // 删除所有字段
        this.deleteField(SON_SEC_GRP_ID);
    }

    /**
     * 分页查询接口字段列表
     *
     * @param secGrpId 安全组ID
     * @return 查询结果
     */
    private Page<SecGrpAclField> listField(Long secGrpId) {
        try {
            // 响应结果数据
            String responseContent = this.mockMvc.perform(this.get(String.format("/%s/%s/fields?page=0&size=100",
                    secGrpId, PERMISSION_ID)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // 响应结果类型
            JavaType responseType = this.objectMapper.getTypeFactory().constructParametricType(Page.class, SecGrpAclField.class);
            // 响应结果数据
            return this.objectMapper.readValue(responseContent, responseType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回空对象
        return new Page<>();
    }

    /**
     * 批量处理
     *
     * @param secGrpId        安全组ID
     * @param secGrpAclFields 处理的安全组字段权限
     */
    private void batchSave(@Nonnull Long secGrpId, @Nonnull List<SecGrpAclField> secGrpAclFields) {
        try {
            this.mockMvc.perform(this.post(String.format("/%s/operate", secGrpId))
                    .content(this.objectMapper.writeValueAsString(secGrpAclFields)))
                    .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建字段权限
     *
     * @param secGrpId         安全组ID
     */
    private void createField(@Nonnull Long secGrpId) {
        // 创建的数据对象
        SecGrpAclField secGrpAclField = new SecGrpAclField();
        secGrpAclField.setPermissionId(PERMISSION_ID);
        secGrpAclField.setFieldId(FILED_ID);
        secGrpAclField.setFieldTypeMeaning(FIELD_TYPE_MEANING);
        secGrpAclField.setPermissionType(PERMISSION_TYPE_HIDE);
        secGrpAclField.set_status(AuditDomain.RecordStatus.create);

        this.batchSave(secGrpId, Collections.singletonList(secGrpAclField));

        // 字段数据
        Page<SecGrpAclField> secGrpAclFields;
        // 查询数据
        secGrpAclFields = this.listField(secGrpId);
        assert CollectionUtils.isNotEmpty(secGrpAclFields);
        secGrpAclFields.forEach(field -> {
            assert secGrpId.equals(field.getSecGrpId());
            assert PERMISSION_ID.equals(field.getPermissionId());
            assert FILED_ID.equals(field.getFieldId());
            assert FIELD_TYPE_MEANING.equals(field.getFieldType());
            assert PERMISSION_TYPE_HIDE.equals(field.getPermissionType());
        });
    }

    /**
     * 更新字段权限
     *
     * @param secGrpId       安全组ID
     */
    private void updateField(@Nonnull Long secGrpId) {
        // 查询字段数据
        Page<SecGrpAclField> secGrpAclFields = this.listField(secGrpId);
        if (CollectionUtils.isNotEmpty(secGrpAclFields)) {
            secGrpAclFields.forEach(secGrpAclField -> {
                secGrpAclField.setPermissionType(PERMISSION_TYPE_ENCRYPTION);
                secGrpAclField.set_status(AuditDomain.RecordStatus.update);
            });

            this.batchSave(secGrpId, secGrpAclFields.getContent());

            // 查询数据
            secGrpAclFields = this.listField(secGrpId);
            if (CollectionUtils.isNotEmpty(secGrpAclFields)) {
                secGrpAclFields.forEach(field -> {
                    assert secGrpId.equals(field.getSecGrpId());
                    assert PERMISSION_TYPE_ENCRYPTION.equals(field.getPermissionType());
                });
            }

        }
    }

    /**
     * 删除字段权限
     *
     * @param secGrpId     安全组ID
     */
    private void deleteField(@Nonnull Long secGrpId) {
        // 查询字段数据
        Page<SecGrpAclField> secGrpAclFields = this.listField(secGrpId);
        if (CollectionUtils.isNotEmpty(secGrpAclFields)) {
            secGrpAclFields.forEach(secGrpAclField -> secGrpAclField.set_status(AuditDomain.RecordStatus.delete));

            this.batchSave(secGrpId, secGrpAclFields.getContent());

            // 查询数据
            secGrpAclFields = this.listField(secGrpId);
            assert CollectionUtils.isEmpty(secGrpAclFields);
        }
    }

    /**
     * 断言删除启用标识
     *
     * @param secGrpAclFields  安全组字段权限
     * @param deleteEnableFlag 删除启用标识 true 可以删除 false 不可以删除
     */
    private void assertDeleteEnableFlag(List<SecGrpAclField> secGrpAclFields, Integer deleteEnableFlag) {
        if (CollectionUtils.isNotEmpty(secGrpAclFields)) {
            secGrpAclFields.forEach(secGrpAclField -> {
                assert deleteEnableFlag.equals(secGrpAclField.getDeleteEnableFlag());
            });
        }
    }
}
