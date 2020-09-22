package org.hzero.iam.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.hzero.core.algorithm.tree.TreeBuilder;
import org.hzero.core.base.BaseConstants;
import org.hzero.iam.api.dto.CompanyOuInvorgDTO;
import org.hzero.iam.api.dto.CompanyOuInvorgNodeDTO;
import org.hzero.iam.api.dto.ResponseCompanyOuInvorgDTO;
import org.hzero.iam.api.dto.RoleAuthDataDTO;
import org.hzero.iam.app.service.RoleAuthDataLineService;
import org.hzero.iam.app.service.RoleAuthDataService;
import org.hzero.iam.domain.entity.RoleAuthData;
import org.hzero.iam.domain.entity.RoleAuthDataLine;
import org.hzero.iam.domain.repository.RoleAuthDataLineRepository;
import org.hzero.iam.domain.repository.RoleAuthDataRepository;
import org.hzero.iam.domain.repository.RoleAuthorityRepository;
import org.hzero.iam.domain.service.AuthorityDomainService;
import org.hzero.iam.domain.service.role.AbstractAuthorityCommonService;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.iam.infra.constant.HiamError;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 角色单据权限管理应用服务默认实现
 *
 * @author qingsheng.chen@hand-china.com 2019-06-14 11:49:29
 */
@Service
public class RoleAuthDataServiceImpl extends AbstractAuthorityCommonService implements RoleAuthDataService {
    public static final Logger logger = LoggerFactory.getLogger(RoleAuthDataServiceImpl.class);
    private static final String ROOT_ID = "-1";
    private RoleAuthDataRepository roleAuthDataRepository;
    private RoleAuthDataLineService roleAuthDataLineService;
    private RoleAuthDataLineRepository dataLineRepository;
    private AuthorityDomainService authorityDomainService;
    private RoleAuthorityRepository roleAuthorityRepository;

    @Autowired
    public RoleAuthDataServiceImpl(RoleAuthDataRepository roleAuthDataRepository,
                                   RoleAuthDataLineService roleAuthDataLineService, AuthorityDomainService authorityDomainService,
                                   RoleAuthorityRepository roleAuthorityRepository, RoleAuthDataLineRepository dataLineRepository) {
        this.roleAuthDataRepository = roleAuthDataRepository;
        this.roleAuthDataLineService = roleAuthDataLineService;
        this.authorityDomainService = authorityDomainService;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.dataLineRepository = dataLineRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleAuthDataDTO pageRoleAuthDataLine(Long tenantId, Long roleId, String authorityTypeCode, String dataCode, String dataName, PageRequest pageRequest) {
        RoleAuthData roleAuthData = queryRoleAuthData(tenantId, roleId, authorityTypeCode);
        return new RoleAuthDataDTO()
                .setRoleAuthData(roleAuthData)
                .setRoleAuthDataLineList(roleAuthDataLineService.pageRoleAuthDataLine(roleAuthData, dataCode, dataName, pageRequest));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleAuthDataDTO createRoleAuthDataLine(RoleAuthDataDTO roleAuthData) {
        if (roleAuthData.getRoleAuthData().getAuthDataId() != null) {
            roleAuthDataRepository.updateOptional(roleAuthData.getRoleAuthData(), RoleAuthData.FIELD_INCLUDE_ALL_FLAG);
        } else {
//            roleAuthDataRepository.insertSelective(roleAuthData.getRoleAuthData());
            saveDefaultRoleAuthData(roleAuthData.getRoleAuthData());
        }
        RoleAuthDataDTO roleAuthDataLine = roleAuthDataLineService.createRoleAuthDataLine(roleAuthData);
        return roleAuthDataLine;
    }

    @Override
    public ResponseCompanyOuInvorgDTO treeRoleAuthority(Long tenantId, Long roleId, String dataCode, String dataName) {
        //查询所有的公司、业务实体、库存组织
        List<CompanyOuInvorgNodeDTO> originDataList = roleAuthDataRepository.listCompanyUoInvorg(tenantId, roleId, dataCode, dataName);
        // 装载数据集合，用于构建树
        List<CompanyOuInvorgDTO> treeDataList = authorityDomainService.generateTreeDataList(originDataList);
        List<CompanyOuInvorgDTO> treeList = TreeBuilder.buildTree(treeDataList, ROOT_ID, new ArrayList<>(256), CompanyOuInvorgDTO.NODE);
        return new ResponseCompanyOuInvorgDTO(treeDataList, treeList);
    }

    @Override
    public List<CompanyOuInvorgDTO> createRoleAuthority(Long tenantId, Long roleId, List<CompanyOuInvorgDTO> companyOuInvorgDTOList) {
        if (companyOuInvorgDTOList == null) {
            companyOuInvorgDTOList = Collections.emptyList();
        }
        List<RoleAuthDataLine> roleAuthDataLineList = new ArrayList<>(256);
        companyOuInvorgDTOList
                .stream()
                .collect(Collectors.groupingBy(CompanyOuInvorgDTO::getTypeCode))
                .forEach((typeCode, list) -> {
                    if (CollectionUtils.isEmpty(list)) {
                        return;
                    }
                    RoleAuthData roleAuthData = roleAuthDataRepository.selectOne(new RoleAuthData().setAuthorityTypeCode(typeCode).setRoleId(roleId).setTenantId(tenantId));
                    if (roleAuthData == null) {
                        logger.debug("Error data : type={},role={},tenant={}", typeCode, roleId, tenantId);
                        roleAuthData = new RoleAuthData().setAuthorityTypeCode(typeCode).setRoleId(roleId).setTenantId(tenantId).setIncludeAllFlag(BaseConstants.Flag.NO);
//                        roleAuthDataRepository.insertSelective(roleAuthData);
                        saveDefaultRoleAuthData(roleAuthData);
                    }
                    roleAuthDataLineList.addAll(create(roleAuthData.getAuthDataId(), tenantId, list));
                });
        List<RoleAuthData> roleAuthDataList = roleAuthDataRepository.selectByCondition(Condition.builder(RoleAuthData.class)
                .andWhere(Sqls.custom()
                        .andEqualTo(RoleAuthData.FIELD_ROLE_ID, roleId)
                        .andEqualTo(RoleAuthData.FIELD_TENANT_ID, tenantId)
                        .andIn(RoleAuthData.FIELD_AUTHORITY_TYPE_CODE, Arrays.asList(Constants.AUTHORITY_TYPE_CODE.COMPANY, Constants.AUTHORITY_TYPE_CODE.OU, Constants.AUTHORITY_TYPE_CODE.INVORG)))
                .build());
        if (!CollectionUtils.isEmpty(roleAuthDataList)) {
            roleAuthDataList.forEach(roleAuthData -> {
                List<RoleAuthDataLine> lineList = roleAuthDataLineService.listRoleAuthDataLine(roleAuthData.getAuthDataId(), roleAuthData.getTenantId());
                if (!CollectionUtils.isEmpty(lineList)) {
                    roleAuthDataLineService.deleteRoleAuthDataLine(lineList);
                }
            });
        }
//        roleAuthDataLineService.batchInsert(roleAuthDataLineList);
        batchSaveDefaultRoleAuthDataLine(roleAuthDataLineList);
        return companyOuInvorgDTOList;
    }

    @Override
    public List<CompanyOuInvorgDTO> createAuthorityForRoles(Long tenantId, List<CompanyOuInvorgDTO> companyOuInvorgDTOList) {
        // 按角色分组
        companyOuInvorgDTOList.stream()
                .collect(Collectors.groupingBy(CompanyOuInvorgDTO::getRoleId))
                .forEach((roleId, list) -> {
                            createRoleAuthority(tenantId, roleId, list);
                        }
                );
        return companyOuInvorgDTOList;
    }

    @Override
    public void copyRoleAuthority(Long organizationId, Long roleId, List<Long> copyRoleIdList) {
        // 获取原有角色的单据维度
        if (CollectionUtils.isEmpty(copyRoleIdList)) {
            throw new CommonException(HiamError.ErrorCode.COPY_ROLE_LIST_NOT_NULL);
        }
        for (Long copyRoleId : copyRoleIdList) {
            // 获取与源单据维度匹配的目标维度
            List<String> compDocTypes = roleAuthorityRepository.selectCompareDimensions(roleId, copyRoleId);
            // 判断被复制的角色的单据维度下是否存在
            if (CollectionUtils.isEmpty(compDocTypes)) {
                throw new CommonException(HiamError.ErrorCode.DOC_TYPE_DIMENSIONS_EMPTY);
            }
            // 存在匹配的权限维度，将匹配维度的权限进行复制, 先判断权限维度头，再复制权限维度行
            for (String compDocType : compDocTypes) {
                // 查询权限维度头信息
                Long authDataId =
                        roleAuthDataRepository.selectRoleAuthDataId(organizationId, roleId, copyRoleId, compDocType);
                if (authDataId != null) {
                    // 获取源角色权限数据:排除目标角色已经存在的权限数据
                    List<RoleAuthDataLine> roleAuthDataLines =
                            dataLineRepository.selectCompliantRoleAuthDatas(organizationId, roleId, copyRoleId, compDocType);
                    if (!CollectionUtils.isEmpty(roleAuthDataLines)) {
                        // 替换authDataId
                        roleAuthDataLines.forEach(roleAuthDataLine -> roleAuthDataLine.setAuthDataId(authDataId));
//                        dataLineRepository.batchInsertSelective(roleAuthDataLines);
                        batchSaveDefaultRoleAuthDataLine(roleAuthDataLines);
                    }
                }
            }
        }

    }

    private List<RoleAuthDataLine> create(long authDataId, Long tenantId, List<CompanyOuInvorgDTO> companyOuInvorgList) {
        List<RoleAuthDataLine> roleAuthDataLineList = new ArrayList<>();
        companyOuInvorgList.forEach(item ->
                roleAuthDataLineList.add(new RoleAuthDataLine()
                        .setAuthDataId(authDataId)
                        .setTenantId(tenantId)
                        .setDataId(item.getDataId())
                        .setDataCode(item.getDataCode())
                        .setDataName(item.getDataName()))
        );
        return roleAuthDataLineList;
    }

    private RoleAuthData queryRoleAuthData(Long tenantId, Long roleId, String authorityTypeCode) {
        RoleAuthData roleAuthData = new RoleAuthData().setTenantId(tenantId).setRoleId(roleId).setAuthorityTypeCode(authorityTypeCode);
        return Optional.ofNullable(roleAuthDataRepository.select(tenantId, roleId, authorityTypeCode))
                .orElse(roleAuthData.setIncludeAllFlag(BaseConstants.Flag.NO));
    }
}
