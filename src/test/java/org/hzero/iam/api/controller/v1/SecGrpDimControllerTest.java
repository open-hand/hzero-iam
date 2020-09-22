package org.hzero.iam.api.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import io.choerodon.core.domain.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.iam.api.dto.SecGrpDclDimDTO;
import org.hzero.iam.api.dto.SecGrpDclDimLineDTO;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecGrpDimController测试类
 *
 * @author bo.he02@hand-china.com 2020/03/20 14:15
 */
public class SecGrpDimControllerTest extends BaseControllerTest {
    /**
     * 日志打印对象
     */
    private static Logger logger = LoggerFactory.getLogger(SecGrpDimControllerTest.class);

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
        return "/v1/%s";
    }

    @Test
    public void test_001_WhenListAssignableDimSuccess() {
        // 模拟登录
        this.login_16304();

        // 查询数据
        Page<SecGrpDclDimDTO> secGrpDclDimDTOS = this.listAssignableDim(0L, SON_SEC_GRP_ID);

        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOS)) {
            secGrpDclDimDTOS.forEach(secGrpDclDimDTO -> {
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = secGrpDclDimDTO.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    secGrpDclDimLineList.forEach(secGrpDclDimLineDTO -> {
                        assert secGrpDclDimDTO.getAuthScopeCode().equals(secGrpDclDimLineDTO.getDimensionType());
                    });
                }
            });
        }
    }

    /**
     * 选择所有数据
     */
    @Test
    public void test_002_WhenCheckAllSuccess() {
        // 模拟登录
        this.login_16304();

        // 初始化数据
        this.init();

        // 子安全组数据权限维度数据
        Page<SecGrpDclDimDTO> sonResponseData;
        // 父安全组数据权限维度数据
        Page<SecGrpDclDimDTO> parentResponseData;

        // 选择子安全组
        this.check(SON_SEC_GRP_ID, 1);
        // 选择父安全组
        this.check(PARENT_SEC_GRP_ID, 1);


        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组数据权限维度数据可编辑性
        this.assertEditFlag(sonResponseData, 0);

        // 查询父安全组数据权限维度数据
        parentResponseData = this.listAssignableDim(0L, PARENT_SEC_GRP_ID);
        // 断言父安全组数据权限维度数据可编辑性
        this.assertEditFlag(parentResponseData, 1);
    }

    /**
     * 取消选择所有数据
     */
    @Test
    public void test_003_WhenUncheckAllSuccess() {
        // 模拟登录
        this.login_16304();

        // 初始化数据即为取消选择所有数据
        this.init();
    }

    @Test
    public void test_004_WhenCheckAllParentSuccess() {
        // 模拟登录
        this.login_16304();

        // 选择父安全组
        this.check(PARENT_SEC_GRP_ID, 1);
    }

    @Test
    public void test_005_WhenUncheckAllParentSuccess() {
        // 模拟登录
        this.login_16304();

        // 不选择父安全组
        this.check(PARENT_SEC_GRP_ID, 0);
    }

    /**
     * 当父安全组选择了之后，子安全组不可编辑
     * 当父安全组取消选择后，子安全组可编辑
     */
    @Test
    public void test_006_WhenBatchSaveSuccess_001() {
        // 模拟登录
        this.login_16304();

        // 初始化数据
        this.init();

        // 子安全组数据权限维度数据
        Page<SecGrpDclDimDTO> sonResponseData;


        // ++++++++++ 1. 当父安全组选择了之后，子安全组不可编辑 +++++++++
        // 选择父安全组
        this.check(PARENT_SEC_GRP_ID, 1);

        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组不可编辑
        this.assertEditFlag(sonResponseData, 0);


        // ++++++++++ 2. 当父安全组取消选择后，子安全组可编辑 +++++++++
        // 取消选择父安全组
        this.check(PARENT_SEC_GRP_ID, 0);

        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组可编辑
        this.assertEditFlag(sonResponseData, 1);
    }

    /**
     * 当子安全组选择后，然后父安全组选择了，子安全组不可编辑
     * 当父安全组取消选择后，子安全组可编辑
     */
    @Test
    public void test_007_WhenBatchSaveSuccess_002() {
        // 模拟登录
        this.login_16304();

        // 初始化数据
        this.init();

        // 子安全组数据权限维度数据
        Page<SecGrpDclDimDTO> sonResponseData;

        // ++++++++++ 1. 当子安全组选择后，然后父安全组选择了，子安全组不可编辑 +++++++++
        // 选择子安全组
        this.check(SON_SEC_GRP_ID, 1);
        // 选择父安全组
        this.check(PARENT_SEC_GRP_ID, 1);

        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组不可编辑
        this.assertEditFlag(sonResponseData, 0);


        // ++++++++++ 2. 当父安全组取消选择后，子安全组可编辑 +++++++++
        // 取消选择父安全组
        this.check(PARENT_SEC_GRP_ID, 0);

        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组可编辑
        this.assertEditFlag(sonResponseData, 1);
    }

    /**
     * 场景1:
     * 1. 子安全组选择了某个数据权限维度(未选择数据权限)
     * 2. 父安全组也选择了该维度[此时子安全组不能选择当前维度下的任何数据权限]
     * 3. 父安全组选择了数据权限[此时子安全组可以选择父安全组选择了的数据权限]
     * 4. 父安全组取消选择该维度[此时父安全组对应的数据权限全部移除,子安全组数据权限保留]
     * <p>
     * 场景2:
     * 1. 子安全组未选择数据权限维度
     * 2. 父安全组选择了数据权限维度[此时子安全组有了该维度,但不能选择该维度下的任何数据]
     * 3. 父安全组选择了数据权限[此时子安全组可以选择父安全组选择了的数据权限]
     * 4. 父安全组取消选择该维度[此时父安全组对应的数据权限全部移除,子安全组对应的数据权限全部移除]
     * <p>
     * (注: 不考虑多个父安全组情况,如果是多个父安全组,则需要根据安全组数据权限交叉的情况进行权限移除)
     */
    @Test
    public void test_008_WhenBatchSaveSuccess() {
        // 模拟登录
        this.login_16304();

        List<SecGrpDclDimDTO> content = this.listAssignableDim(0L, 237L).getContent();

        List<SecGrpDclDimDTO> secGrpDclDimDTOList = new ArrayList<>(1);
        for (SecGrpDclDimDTO secGrpDclDimDTO : content) {
            if (57 == secGrpDclDimDTO.getDocTypeId() && "BIZ".equals(secGrpDclDimDTO.getAuthScopeCode())) {
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = secGrpDclDimDTO.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    for (SecGrpDclDimLineDTO secGrpDclDimLineDTO : secGrpDclDimLineList) {
                        if ("PURCHASE_ORGANIZATION".equals(secGrpDclDimLineDTO.getAuthTypeCode())) {
                            if (secGrpDclDimLineDTO.getDeleteEnableFlag() == 1 && secGrpDclDimLineDTO.getSecGrpDclDimLineCheckedFlag() == 1) {
                                secGrpDclDimLineDTO.setSecGrpDclDimLineCheckedFlag(0);
                                secGrpDclDimDTOList.add(secGrpDclDimDTO);
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }

        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            this.batchSave(0L, 237L, secGrpDclDimDTOList);
        }
    }

    /**
     * 场景1:
     * 1. 子安全组选择了某个数据权限维度,并选择了数据权限
     * 2. 父安全组也选择了该维度[此时子安全组已选择的数据权限被移除,且不能选择当前维度下的任何数据权限]
     * 3. 父安全组选择了数据权限[此时子安全组可以选择父安全组选择了的数据权限]
     * 4. 父安全组取消选择该维度[此时父安全组对应的数据权限全部移除,子安全组数据权限保留]
     * <p>
     * (注: 不考虑多个父安全组情况,如果是多个父安全组,则需要根据安全组数据权限交叉的情况进行权限移除)
     */
    @Test
    public void test_009_WhenBatchSaveSuccess() {
        // 模拟登录
        this.login_16304();

        List<SecGrpDclDimDTO> content = this.listAssignableDim(0L, 237L).getContent();

        List<SecGrpDclDimDTO> secGrpDclDimDTOList = new ArrayList<>(1);
        for (SecGrpDclDimDTO secGrpDclDimDTO : content) {
            if (57 == secGrpDclDimDTO.getDocTypeId() && "BIZ".equals(secGrpDclDimDTO.getAuthScopeCode())) {
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = secGrpDclDimDTO.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    for (SecGrpDclDimLineDTO secGrpDclDimLineDTO : secGrpDclDimLineList) {
                        if ("PURCHASE_ORGANIZATION".equals(secGrpDclDimLineDTO.getAuthTypeCode())) {
                            if (secGrpDclDimLineDTO.getDeleteEnableFlag() == 1 && secGrpDclDimLineDTO.getSecGrpDclDimLineCheckedFlag() == 0) {
                                secGrpDclDimLineDTO.setSecGrpDclDimLineCheckedFlag(1);
                                secGrpDclDimDTOList.add(secGrpDclDimDTO);
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }

        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            this.batchSave(0L, 237L, secGrpDclDimDTOList);
        }
    }

    @Test
    public void test_010_whenUnCheckSuccess() {
        // 模拟登录
        this.login_16304();

        List<SecGrpDclDimDTO> content = this.listAssignableDim(0L, 237L).getContent();

        List<SecGrpDclDimDTO> secGrpDclDimDTOList = new ArrayList<>(1);
        for (SecGrpDclDimDTO secGrpDclDimDTO : content) {
            if (57 == secGrpDclDimDTO.getDocTypeId() && "BIZ".equals(secGrpDclDimDTO.getAuthScopeCode())) {
                secGrpDclDimDTO.setSecGrpDclDimCheckedFlag(0);
                secGrpDclDimDTO.setSecGrpDclDimLineList(null);
                secGrpDclDimDTOList.add(secGrpDclDimDTO);
                break;
            }
        }

        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            this.batchSave(0L, 237L, secGrpDclDimDTOList);
        }
    }

    /**
     * 初始化数据
     */
    private void init() {
        // 选择标识
        Integer checkFlag = 0;
        // 父安全组数据权限维度数据
        Page<SecGrpDclDimDTO> parentResponseData;
        // 子安全组数据权限维度数据
        Page<SecGrpDclDimDTO> sonResponseData;

        // ++++++++++ 1. 初始化父安全组数据 +++++++++
        // 初始化父安全组
        this.check(PARENT_SEC_GRP_ID, checkFlag);

        // ++++++++++ 2. 初始化子安全组数据 +++++++++
        // 初始化子安全组
        this.check(SON_SEC_GRP_ID, checkFlag);


        // ++++++++++ 3. 校验初始化结果 +++++++++
        // 查询父安全组数据权限维度数据
        parentResponseData = this.listAssignableDim(0L, PARENT_SEC_GRP_ID);
        // 断言父安全组数据权限维度数据可编辑性
        this.assertEditFlag(parentResponseData, 1);

        // 查询子安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, SON_SEC_GRP_ID);
        // 断言子安全组数据权限维度数据可编辑性
        this.assertEditFlag(sonResponseData, 1);
    }

    /**
     * 操作安全组数据，选择或取消选择
     *
     * @param secGrpId  安全组
     * @param checkFlag 选择标识 1 选择 0 不选择
     */
    private void check(Long secGrpId, Integer checkFlag) {
        // 查询安全组数据权限维度数据
        Page<SecGrpDclDimDTO> sonResponseData = this.listAssignableDim(0L, secGrpId);
        // 初始化安全组数据权限维度数据
        this.setCheckFlag(sonResponseData, checkFlag);
        // 提交安全组数据
        this.batchSave(0L, secGrpId, sonResponseData);

        // 查询安全组数据权限维度数据
        sonResponseData = this.listAssignableDim(0L, secGrpId);
        // 断言安全组数据权限维度数据是否已选择
        this.assertCheckFlag(sonResponseData, checkFlag);
    }

    /**
     * 查询安全组数据权限维度数据
     *
     * @param organizationId 租户ID
     * @param secGrpId       安全组ID
     * @return 安全组数据权限维度数据
     */
    private Page<SecGrpDclDimDTO> listAssignableDim(Long organizationId, Long secGrpId) {
        try {
            // 查询安全组数据
            String responseContent = this.mockMvc.perform(this.get(String.format("/%s/%s/sec-grp-dcl-dims?page=0&size=100",
                    organizationId, secGrpId)))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn().getResponse().getContentAsString();

            // 响应结果类型
            JavaType responseType = this.objectMapper.getTypeFactory().constructParametricType(Page.class, SecGrpDclDimDTO.class);
            // 响应结果数据
            return this.objectMapper.readValue(responseContent, responseType);
        } catch (Exception e) {
            logger.error("List Assignable Dim Failure", e);
        }

        // 返回空对象
        return new Page<>();
    }

    /**
     * 批量新增或者更新安全组数据权限维度列表
     *
     * @param organizationId      租户
     * @param secGrpId            安全组ID
     * @param secGrpDclDimDTOPage 安全组数据权限维度数据
     */
    private void batchSave(Long organizationId, Long secGrpId, Page<SecGrpDclDimDTO> secGrpDclDimDTOPage) {
        // 提交数据
        this.batchSave(organizationId, secGrpId, secGrpDclDimDTOPage.getContent());
    }

    /**
     * 批量新增或者更新安全组数据权限维度列表
     *
     * @param organizationId      租户
     * @param secGrpId            安全组ID
     * @param secGrpDclDimDTOList 安全组数据权限维度数据
     */
    private void batchSave(Long organizationId, Long secGrpId, List<SecGrpDclDimDTO> secGrpDclDimDTOList) {
        // 提交数据
        try {
            this.batchSave(organizationId, secGrpId, this.objectMapper.writeValueAsString(secGrpDclDimDTOList));
        } catch (JsonProcessingException e) {
            logger.error("Batch Save Object Mapper Write Value As String Failure", e);
        }
    }

    /**
     * 批量新增或者更新安全组数据权限维度列表
     *
     * @param organizationId 租户
     * @param secGrpId       安全组ID
     * @param requestContent 请求内容
     */
    private void batchSave(Long organizationId, Long secGrpId, String requestContent) {
        // 提交数据
        try {
            this.mockMvc.perform(this.post(String.format("/%s/%s/sec-grp-dcl-dims", organizationId, secGrpId))
                    .content(requestContent))
                    .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            logger.error("Batch Save Failure", e);
        }
    }

    /**
     * 设置选择标识
     *
     * @param secGrpDclDimDTOList 安全组数据权限维度数据
     * @param checkFlag           选择标识  1 选择 0 取消
     */
    private void setCheckFlag(List<SecGrpDclDimDTO> secGrpDclDimDTOList, Integer checkFlag) {
        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            secGrpDclDimDTOList.forEach(responseItem -> {
                responseItem.setSecGrpDclDimCheckedFlag(checkFlag);
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = responseItem.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    secGrpDclDimLineList.forEach(secGrpDclDimLineDTO -> secGrpDclDimLineDTO.setSecGrpDclDimLineCheckedFlag(checkFlag));
                }
            });
        }
    }

    /**
     * 断言选择标识
     *
     * @param secGrpDclDimDTOList 安全组数据权限维度数据
     * @param checkedFlag         断言选择标识  1 已选择 0 未选择
     */
    private void assertCheckFlag(List<SecGrpDclDimDTO> secGrpDclDimDTOList, Integer checkedFlag) {
        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            secGrpDclDimDTOList.forEach(responseItem -> {
                assert checkedFlag.equals(responseItem.getSecGrpDclDimCheckedFlag());
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = responseItem.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    secGrpDclDimLineList.forEach(secGrpDclDimLineDTO -> {
                        assert checkedFlag.equals(secGrpDclDimLineDTO.getSecGrpDclDimLineCheckedFlag());
                    });
                }
            });
        }
    }

    /**
     * 断言编辑标识
     *
     * @param secGrpDclDimDTOList 安全组数据权限维度数据
     * @param editFlag            断言编辑标识  1 可编辑 0 不可编辑
     */
    private void assertEditFlag(List<SecGrpDclDimDTO> secGrpDclDimDTOList, Integer editFlag) {
        if (CollectionUtils.isNotEmpty(secGrpDclDimDTOList)) {
            secGrpDclDimDTOList.forEach(responseItem -> {
                assert editFlag.equals(responseItem.getEditEnableFlag());
                List<SecGrpDclDimLineDTO> secGrpDclDimLineList = responseItem.getSecGrpDclDimLineList();
                if (CollectionUtils.isNotEmpty(secGrpDclDimLineList)) {
                    secGrpDclDimLineList.forEach(secGrpDclDimLineDTO -> {
                        assert editFlag.equals(secGrpDclDimLineDTO.getDeleteEnableFlag());
                    });
                }
            });
        }
    }
}
