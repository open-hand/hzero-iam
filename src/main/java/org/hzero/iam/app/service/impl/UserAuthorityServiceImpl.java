package org.hzero.iam.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.algorithm.tree.TreeBuilder;
import org.hzero.core.base.BaseConstants;
import org.hzero.iam.api.dto.CompanyOuInvorgDTO;
import org.hzero.iam.api.dto.CompanyOuInvorgNodeDTO;
import org.hzero.iam.api.dto.ResponseCompanyOuInvorgDTO;
import org.hzero.iam.api.dto.UserAuthorityDTO;
import org.hzero.iam.app.service.UserAuthorityService;
import org.hzero.iam.domain.entity.UserAuthority;
import org.hzero.iam.domain.entity.UserAuthorityLine;
import org.hzero.iam.domain.repository.UserAuthorityLineRepository;
import org.hzero.iam.domain.repository.UserAuthorityRepository;
import org.hzero.iam.domain.service.AuthorityDomainService;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 用户权限管理表应用服务默认实现
 *
 * @author liang.jin@hand-china.com 2018-07-31 15:45:42
 */
@Service
public class UserAuthorityServiceImpl implements UserAuthorityService {

    @Autowired
    UserAuthorityRepository userAuthorityRepository;
    @Autowired
    UserAuthorityLineRepository userAuthorityLineRepository;
    @Autowired
    AuthorityDomainService authorityDomainService;

    private static final String ROOT_ID = "-1";

    @Override
    public ResponseCompanyOuInvorgDTO listComanyOuInvorg(Long tenantId, Long userId, String dataCode, String dataName) {
        //查询所有的公司、业务实体、库存组织
        List<CompanyOuInvorgNodeDTO> originDataList = userAuthorityRepository.listComanyUoInvorg(tenantId, userId, dataCode, dataName);
        // 装载数据集合，用于构建树
        List<CompanyOuInvorgDTO> treeDataList = authorityDomainService.generateTreeDataList(originDataList);
        //构建树
        List<CompanyOuInvorgDTO> treeList = TreeBuilder.buildTree(treeDataList, ROOT_ID, new ArrayList<>(), CompanyOuInvorgDTO.NODE);
        return new ResponseCompanyOuInvorgDTO(treeDataList, treeList);
    }

    @Override
    public ResponseCompanyOuInvorgDTO listComanyOuInvorgAll(Long tenantId, String dataCode, String dataName) {
        //查询所有的公司、业务实体、库存组织
        List<CompanyOuInvorgNodeDTO> originDataList = userAuthorityRepository.listComanyUoInvorgAll(tenantId, dataCode, dataName);
        // 装载数据集合，用于构建树
        List<CompanyOuInvorgDTO> treeDataList = authorityDomainService.generateTreeDataList(originDataList);
        //构建树
        List<CompanyOuInvorgDTO> treeList = TreeBuilder.buildTree(treeDataList, ROOT_ID, new ArrayList<>(), CompanyOuInvorgDTO.NODE);
        return new ResponseCompanyOuInvorgDTO(treeDataList, treeList);
    }

    @Override
    public void batchDeleteUserAuthorityLines(Long tenantId, Long userId, List<UserAuthorityLine> userAuthorityLineList) {
        if (CollectionUtils.isEmpty(userAuthorityLineList)) {
            return;
        }
//        userAuthorityLineRepository.batchDeleteByPrimaryKey(userAuthorityLineList);
//        if (userAuthorityLineRepository.selectCount(new UserAuthorityLine().setAuthorityId(userAuthorityLineList.get(0).getAuthorityId())) == 0) {
//            userAuthorityRepository.deleteByPrimaryKey(userAuthorityLineList.get(0).getAuthorityId());
//        }
        for (UserAuthorityLine userAuthorityLine : userAuthorityLineList) {
            UserAuthorityLine hasUserAuthority = userAuthorityLineRepository.selectByPrimaryKey(userAuthorityLine.getAuthorityLineId());
            if (hasUserAuthority != null) {
                if (StringUtils.equals(hasUserAuthority.getDataSource(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE)) {
                    //默认的用户权限
                    userAuthorityLineRepository.deleteByPrimaryKey(userAuthorityLine.getAuthorityLineId());
                } else if (StringUtils.equals(hasUserAuthority.getDataSource(), Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE)) {
                    hasUserAuthority.setDataSource(Constants.SecGrpAssign.SEC_GRP_DATA_SOURCE);
                    userAuthorityLineRepository.updateByPrimaryKeySelective(hasUserAuthority);
                }
            }
        }

        int lineCount = userAuthorityLineRepository.selectCountByCondition(Condition.builder(UserAuthorityLine.class)
                .andWhere(Sqls.custom()
                        .andEqualTo(UserAuthorityLine.FIELD_AUTHORITY_ID, userAuthorityLineList.get(0).getAuthorityId())
                        .andIn(UserAuthorityLine.FIELD_DATA_SOURCE, Arrays.asList(Constants.SecGrpAssign.DEFAULT_DATA_SOURCE, Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE)))
                .build());
        if (lineCount == 0) {
            Long authorityId = userAuthorityLineList.get(0).getAuthorityId();
            UserAuthority userAuthority = userAuthorityRepository.selectByPrimaryKey(authorityId);
            if (userAuthority != null) {
                if (StringUtils.equals(userAuthority.getDataSource(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE)) {
                    userAuthorityRepository.deleteByPrimaryKey(authorityId);
                } else if (StringUtils.equals(userAuthority.getDataSource(), Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE)) {
                    userAuthority.setDataSource(Constants.SecGrpAssign.SEC_GRP_DATA_SOURCE);
                    userAuthorityRepository.updateByPrimaryKeySelective(userAuthority);
                }
                // 处理用户缓存
                userAuthorityRepository.processUserAuthorityCache(tenantId, userId, userAuthority.getAuthorityTypeCode());
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAuthorityDTO batchCreateUserAuthority(Long tenantId, Long userId, String authorityTypeCode, UserAuthorityDTO userAuthorityDTO) {
        //获取头信息，存在更新，不存在新增
        UserAuthority userAuthority = userAuthorityDTO.getUserAuthority();
        userAuthority.setTenantId(tenantId);
        userAuthority.setUserId(userId);
        userAuthority.setAuthorityTypeCode(authorityTypeCode);
        if (userAuthority.getAuthorityId() != null) {
            userAuthorityRepository.updateOptional(userAuthority, UserAuthority.FIELD_INCLUDE_ALL_FLAG);
        } else {
            userAuthority.setDataSource(Constants.SecGrpAssign.DEFAULT_DATA_SOURCE);
            if (userAuthorityRepository.insert(userAuthority) != 1) {
                throw new CommonException(BaseConstants.ErrorCode.ERROR);
            }
        }
        //获取行信息列表，新增
        List<UserAuthorityLine> userAuthorityLineList = userAuthorityDTO.getUserAuthorityLineList();
        if (CollectionUtils.isNotEmpty(userAuthorityLineList)) {
            for (UserAuthorityLine userAuthorityLine : userAuthorityLineList) {
                userAuthorityLine.setTenantId(tenantId);
                userAuthorityLine.setAuthorityId(userAuthority.getAuthorityId());
                this.checkUserAuthorityLineRepeat(userAuthorityLine);
                userAuthorityLine.setDataSource(Constants.SecGrpAssign.DEFAULT_DATA_SOURCE);
                userAuthorityLineRepository.insert(userAuthorityLine);
            }

        }
        // 如果行为空 并且头不包含所有数据 删除头
        if (BaseConstants.Flag.NO.equals(userAuthority.getIncludeAllFlag()) && CollectionUtils.isEmpty(userAuthorityLineList)) {
            UserAuthority userAuthorityDel = new UserAuthority();
            userAuthorityDel.setAuthorityId(userAuthority.getAuthorityId());
            userAuthorityDel.setDataSource(Constants.SecGrpAssign.DEFAULT_DATA_SOURCE);
            userAuthorityRepository.delete(userAuthorityDel);
//            userAuthorityRepository.deleteByPrimaryKey(userAuthority.getAuthorityId());
        }
        // 处理用户权限数据缓存
        userAuthorityRepository.processUserAuthorityCache(tenantId, userId, authorityTypeCode);
        return userAuthorityDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<UserAuthorityDTO> batchCreateUserAuthority(List<UserAuthorityDTO> userAuthorityDtos) {

        List<UserAuthorityDTO> result = new ArrayList<>();

        for (UserAuthorityDTO userAuthorityDTO : userAuthorityDtos) {
            result.add(this.batchCreateUserAuthority(userAuthorityDTO.getUserAuthority().getTenantId(),
                    userAuthorityDTO.getUserAuthority().getUserId(),
                    userAuthorityDTO.getUserAuthority().getAuthorityTypeCode(),
                    userAuthorityDTO));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyUserAuthority(Long tenantId, Long userId, List<Long> copyUserIdList) {
        //用户的权限头信息列表
        List<String> typeCodeList = userAuthorityRepository.listUserAuthorityTypeCode(tenantId, userId);
        if (CollectionUtils.isNotEmpty(typeCodeList) && CollectionUtils.isNotEmpty(copyUserIdList)) {
            for (Long copyUserId : copyUserIdList) {
                for (String typeCode : typeCodeList) {
                    UserAuthority userAuthority = new UserAuthority();
                    //用户的权限头ID
                    Long authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, typeCode, userAuthorityRepository);
                    //需要复制的权限头ID
                    Long copyAuthorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, copyUserId, typeCode, userAuthorityRepository);
                    //更新数据
                    userAuthorityLineRepository.updateUserAuthorityLine(tenantId, authorityId, copyAuthorityId);
                }
            }
            // 确保数据插入完成后，循环处理缓存，防止产生脏数据
            for (Long copyUserId : copyUserIdList) {
                for (String typeCode : typeCodeList) {
                    userAuthorityRepository.processUserAuthorityCache(tenantId, copyUserId, typeCode);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserAuthority> exchangeUserAuthority(Long tenantId, Long userId, Long exchangeUserId) {
        //如果用户ID和交换的用户ID则不做操作
        if (userId.equals(exchangeUserId)) {
            return null;
        }
        //用户交换后的权限列表
        List<UserAuthority> userAuthorities = null;
        UserAuthority userAuthority = new UserAuthority();
        //查询用户的权限列表头信息
        UserAuthority user = userAuthority.init(userId, tenantId, null, null);
        List<UserAuthority> userAuthorityList = userAuthorityRepository.select(user);
//        List<UserAuthority> userAuthorityList = userAuthorityRepository.listByUserIdAndTenantId(userId, tenantId);
        //查询需要交换用户的权限头信息
        UserAuthority exchangeUser = userAuthority.init(exchangeUserId, tenantId, null, null);
        List<UserAuthority> exchangeUserAuthorityList = userAuthorityRepository.select(exchangeUser);
//        List<UserAuthority> exchangeUserAuthorityList = userAuthorityRepository.listByUserIdAndTenantId(exchangeUserId, tenantId);
        //如果用户没有权限不做任何操作
        if (CollectionUtils.isEmpty(userAuthorityList) && CollectionUtils.isEmpty(exchangeUserAuthorityList)) {
            return null;
        }

        /**
         * 临时保存权限的userId
         */
        Long tempUserId = -exchangeUserId;
        if (CollectionUtils.isNotEmpty(exchangeUserAuthorityList)) {
            exchangeUserAuthorityList.forEach(item -> {
                item.setUserId(tempUserId);
                userAuthorityRepository.updateOptional(item, UserAuthority.FIELD_USER_ID);
            });
        }

        //交换UserID
        if (CollectionUtils.isNotEmpty(userAuthorityList)) {
            userAuthorityList.forEach(u -> u.setUserId(exchangeUserId));
            userAuthorities = userAuthorityRepository.batchUpdateByPrimaryKey(userAuthorityList);
//            handlerExchangeUserAuth(userAuthorityList);
        }

        //交换UserID
        if (CollectionUtils.isNotEmpty(exchangeUserAuthorityList)) {
            exchangeUserAuthorityList.forEach(e -> e.setUserId(userId));
            userAuthorityRepository.batchUpdateByPrimaryKey(exchangeUserAuthorityList);
//            handlerExchangeUserAuth(exchangeUserAuthorityList);
        }
        return userAuthorities;
    }

    @Override
    public UserAuthorityDTO selectCreateUserAuthority(Long tenantId, Long userId, String authorityTypeCode, String dataCode, String dataName, PageRequest pageRequest) {
        UserAuthorityDTO userAuthorityDTO = new UserAuthorityDTO();
        Page<UserAuthorityLine> userAuthorityLines;
        UserAuthority userAuthority = getAuthority(tenantId, userId, authorityTypeCode);
        //查询头ID是否存在
        Long authorityId = userAuthority.getAuthorityId();
        if (authorityId != null && StringUtils.contains(userAuthority.getDataSource(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE)) {
            //查询头数据
            userAuthority = userAuthorityRepository.selectByPrimaryKey(authorityId);
            //分页查询行数据
            userAuthorityLines = userAuthorityLineRepository.selectCreateUserAuthorityLines(authorityId, tenantId, dataCode, dataName, pageRequest);
        } else {
            userAuthority.setIncludeAllFlag(BaseConstants.Flag.NO);
            userAuthorityLines = new Page<>();
        }
        userAuthorityDTO.setUserAuthority(userAuthority);
        userAuthorityDTO.setUserAuthorityLineList(userAuthorityLines);
        return userAuthorityDTO;
    }

    /**
     * 根据 租户ID、用户ID、权限类型代码、查询头表ID没查到则新增后返回主键
     *
     * @param tenantId
     * @param userId
     * @param authorityTypeCode
     * @return
     */
    public UserAuthority getAuthority(Long tenantId, Long userId, String authorityTypeCode) {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setTenantId(tenantId);
        userAuthority.setUserId(userId);
        userAuthority.setAuthorityTypeCode(authorityTypeCode);
        List<UserAuthority> result = userAuthorityRepository.select(userAuthority);
        if (result.size() > 0) {
            return result.get(0);
        } else {
            return userAuthority;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CompanyOuInvorgDTO> createTreeUserAuthority(Long tenantId, Long userId, List<CompanyOuInvorgDTO> companyOuInvorgDTOList) {
        //对数据分组
        Map<String, List<CompanyOuInvorgDTO>> collect = companyOuInvorgDTOList.stream().collect(Collectors.groupingBy(CompanyOuInvorgDTO::getTypeCode));
        List<CompanyOuInvorgDTO> companyList = collect.get(Constants.AUTHORITY_TYPE_CODE.COMPANY);
        List<CompanyOuInvorgDTO> ouList = collect.get(Constants.AUTHORITY_TYPE_CODE.OU);
        List<CompanyOuInvorgDTO> invorgList = collect.get(Constants.AUTHORITY_TYPE_CODE.INVORG);
        //最终保存的数据
        List<UserAuthorityLine> userAuthorityLineList = new ArrayList<>();
        //新增公司类型权限数据
        if (CollectionUtils.isNotEmpty(companyList)) {
            UserAuthority userAuthority = new UserAuthority();
            Long authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.COMPANY, userAuthorityRepository);

            for (CompanyOuInvorgDTO company : companyList) {
                userAuthorityLineList.add(new UserAuthorityLine(authorityId, tenantId, company.getDataId(), company.getDataCode(), company.getDataName(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE));
            }
        }
        //新增业务实体类型权限数据
        if (CollectionUtils.isNotEmpty(ouList)) {
            UserAuthority userAuthority = new UserAuthority();
            Long authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.OU, userAuthorityRepository);
            for (CompanyOuInvorgDTO ou : ouList) {
                userAuthorityLineList.add(new UserAuthorityLine(authorityId, tenantId, ou.getDataId(), ou.getDataCode(), ou.getDataName(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE));
            }
        }

        //新增库存组织类型权限数据
        if (CollectionUtils.isNotEmpty(invorgList)) {
            UserAuthority userAuthority = new UserAuthority();
            Long authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.INVORG, userAuthorityRepository);
            for (CompanyOuInvorgDTO invorg : invorgList) {
                userAuthorityLineList.add(new UserAuthorityLine(authorityId, tenantId, invorg.getDataId(), invorg.getDataCode(), invorg.getDataName(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE));
            }
        }

        //删除以前的数据
        this.deleteOrldDate(tenantId, userId, userAuthorityLineList);

        //保存最终数据
//        userAuthorityLineRepository.batchInsert(userAuthorityLineList);
        saveDefaultUserAuthLine(userAuthorityLineList);
        // 处理公司、业务实体、库存组织的用户权限数据缓存
        userAuthorityRepository.processUserAuthorityCache(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.COMPANY);
        userAuthorityRepository.processUserAuthorityCache(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.OU);
        userAuthorityRepository.processUserAuthorityCache(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.INVORG);
        return companyOuInvorgDTOList;
    }


    /**
     * 根据租户ID和用户ID删除用户权限数据
     *
     * @param tenantId
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrldDate(Long tenantId, Long userId, List<UserAuthorityLine> userAuthorityLineList) {
        UserAuthority userAuthority = new UserAuthority();
        Long authorityId = null;

        authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.COMPANY, userAuthorityRepository);
        //删除行表中公司类型的数据
        if (authorityId != null) {
            this.deleteUserAuthorityLinesByAuthorityId(tenantId, authorityId);
            if (CollectionUtils.isEmpty(userAuthorityLineList)) {
                // 如果没有行删除头
//                userAuthorityRepository.deleteByPrimaryKey(authorityId);
                removeDefaultUserAuth(authorityId);
            }
        }
        //删除行表中业务实体类型的数据
        authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.OU, userAuthorityRepository);
        if (authorityId != null) {
            this.deleteUserAuthorityLinesByAuthorityId(tenantId, authorityId);
            if (CollectionUtils.isEmpty(userAuthorityLineList)) {
                // 如果没有行删除头
//                userAuthorityRepository.deleteByPrimaryKey(authorityId);
                removeDefaultUserAuth(authorityId);
            }
        }
        //删除行表中库存组织类型的数据
        authorityId = userAuthority.getAuthorityIdByTenantIdAndUserId(tenantId, userId, Constants.AUTHORITY_TYPE_CODE.INVORG, userAuthorityRepository);
        if (authorityId != null) {
            this.deleteUserAuthorityLinesByAuthorityId(tenantId, authorityId);
            if (CollectionUtils.isEmpty(userAuthorityLineList)) {
                // 如果没有行删除头
//                userAuthorityRepository.deleteByPrimaryKey(authorityId);
                removeDefaultUserAuth(authorityId);
            }
        }


    }

    /**
     * 唯一索引校验
     *
     * @param userAuthorityLine
     * @return
     * @author liang.jin@hand-china.com 2018-07-09 19:12
     */
    private void checkUserAuthorityLineRepeat(UserAuthorityLine userAuthorityLine) {
        UserAuthorityLine temp = new UserAuthorityLine();
        temp.setAuthorityId(userAuthorityLine.getAuthorityId());
        temp.setDataId(userAuthorityLine.getDataId());
        List<UserAuthorityLine> exists = userAuthorityLineRepository.select(temp);
        if (!exists.isEmpty()) {
            throw new CommonException(BaseConstants.ErrorCode.ERROR_CODE_REPEAT);
        }
    }

    /**
     * 移除用户权限
     *
     * @param authorityId 用户权限Id
     */
    private void removeDefaultUserAuth(Long authorityId) {
        if (authorityId == null) {
            return;
        }
        UserAuthority userAuthority = userAuthorityRepository.selectByPrimaryKey(authorityId);
        removeDefaultUserAuth(userAuthority);
    }

    /**
     * 移除用户权限
     *
     * @param userAuth 用户权限
     */
    private void removeDefaultUserAuth(UserAuthority userAuth) {
        if (userAuth == null || userAuth.getAuthorityId() == null
                || StringUtils.isEmpty(userAuth.getDataSource())
                || userAuth.getObjectVersionNumber() == null) {
            return;
        }
        if (StringUtils.equals(userAuth.getDataSource(), Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE)) {
            userAuth.setDataSource(Constants.SecGrpAssign.SEC_GRP_DATA_SOURCE);
            userAuthorityRepository.updateByPrimaryKeySelective(userAuth);
        } else if (StringUtils.equals(userAuth.getDataSource(), Constants.SecGrpAssign.DEFAULT_DATA_SOURCE)) {
            userAuthorityRepository.deleteByPrimaryKey(userAuth);
        }
    }

    /**
     * 根据authorityId删除行表数据
     *
     * @param authorityId
     * @param tenantId
     */
    private void deleteUserAuthorityLinesByAuthorityId(Long tenantId, Long authorityId) {
        UserAuthorityLine temp = new UserAuthorityLine();
        temp.setAuthorityId(authorityId);
        temp.setTenantId(tenantId);
        List<UserAuthorityLine> hasUserAuthLines = userAuthorityLineRepository.select(temp);
        if (CollectionUtils.isNotEmpty(hasUserAuthLines)) {
//            userAuthorityLineRepository.delete(temp);
            for (UserAuthorityLine userAuthLine : hasUserAuthLines) {
                removeDefaultUserAuthLine(userAuthLine);
            }
        }
    }

    /**
     * 移除默认的用户权限行
     *
     * @param userAuthLine 用户权限行
     */
    private void removeDefaultUserAuthLine(UserAuthorityLine userAuthLine) {
        if (userAuthLine == null || userAuthLine.getAuthorityLineId() == null
                || userAuthLine.getObjectVersionNumber() == null
                || StringUtils.isEmpty(userAuthLine.getDataSource())) {
            return;
        }

        if (StringUtils.equals(Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE, userAuthLine.getDataSource())) {
            userAuthLine.setDataSource(Constants.SecGrpAssign.SEC_GRP_DATA_SOURCE);
            userAuthorityLineRepository.updateByPrimaryKeySelective(userAuthLine);
        } else if (StringUtils.equals(Constants.SecGrpAssign.DEFAULT_DATA_SOURCE, userAuthLine.getDataSource())) {
            userAuthorityLineRepository.deleteByPrimaryKey(userAuthLine);
        }
    }

    /**
     * 批量保存用户权限行
     *
     * @param userAuthLines
     */
    private void saveDefaultUserAuthLine(List<UserAuthorityLine> userAuthLines) {
        if (userAuthLines != null) {
            for (UserAuthorityLine userAuthLine : userAuthLines) {
                UserAuthorityLine userAuthLineArgs = new UserAuthorityLine();
                userAuthLineArgs.setAuthorityId(userAuthLine.getAuthorityId());
                userAuthLineArgs.setDataId(userAuthLine.getDataId());
                UserAuthorityLine hasUserAuthLine = userAuthorityLineRepository.selectOne(userAuthLineArgs);
                if (hasUserAuthLine != null) {
                    hasUserAuthLine.setDataSource(Constants.SecGrpAssign.DEFAULT_SEC_GRP_DATA_SOURCE);
                    userAuthorityLineRepository.updateByPrimaryKeySelective(hasUserAuthLine);
                } else {
                    userAuthorityLineRepository.insertSelective(userAuthLine);
                }
            }
        }
    }

//    private void handlerExchangeUserAuth(List<UserAuthority> userAuthorities) {
//        if (userAuthorities == null) return;
//        for (UserAuthority hasUserAuth : userAuthorities) {
//            UserAuthority userAuthArgs = new UserAuthority();
//            userAuthArgs.setTenantId(hasUserAuth.getTenantId());
//            userAuthArgs.setUserId(hasUserAuth.getUserId());
//            userAuthArgs.setAuthorityTypeCode(hasUserAuth.getAuthorityTypeCode());
//            UserAuthority userAuth = userAuthorityRepository.selectOne(userAuthArgs);
//            if (userAuth != null && StringUtils.contains(userAuth.getDataSource(), Constants.SecGrpAssign.SEC_GRP_DATA_SOURCE)) {
//                //A用户包含LOV的默认权限，B用户包含LOV的安全组权限，在权限分配时: 安全组不进行交互，
//                //则当A 用户的LOV默认权限，交换成B的LOV权限时，需要和安全组整合在一起,同时，用户权限行也需要更改
//                //删除用户权限行
//                userAuthorityRepository.deleteByPrimaryKey(userAuth.getAuthorityId());
//                //查出A、B两个用户行权限
//                UserAuthorityLine userAuthA = new UserAuthorityLine();
//                userAuthA.setAuthorityId(userAuth.getAuthorityId());
//                List<UserAuthorityLine> userAuthLines = userAuthorityLineRepository.select(userAuthA);
//                userAuthorityLineRepository.batchDelete(userAuthLines);
//
//                hasUserAuth.setDataSource(Constants.SecGrpAssign.DEFALUT_SEC_GRP_DATA_SOUCE);
//                userAuthorityRepository.updateByPrimaryKeySelective(hasUserAuth);
//                userAuthorityLineRepository.duplicateLineDeal(userAuthLines, hasUserAuth.getAuthorityId());
//            } else {
//                userAuthorityRepository.updateByPrimaryKey(hasUserAuth);
//            }
//        }
//    }

}
