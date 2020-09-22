package org.hzero.iam.infra.repository.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.common.HZeroService;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.redis.safe.SafeRedisHelper;
import org.hzero.iam.domain.entity.Permission;
import org.hzero.iam.domain.repository.PermissionRepository;
import org.hzero.iam.domain.vo.Lov;
import org.hzero.iam.domain.vo.PermissionCacheVO;
import org.hzero.iam.domain.vo.PermissionVO;
import org.hzero.iam.infra.mapper.PermissionMapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 权限管理
 *
 * @author allen 2018/6/25
 */
@Repository
public class PermissionRepositoryImpl extends BaseRepositoryImpl<Permission> implements PermissionRepository {

    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RedisHelper redisHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionRepositoryImpl.class);

    @Override
    public Page<PermissionVO> pagePermission(String condition, String level, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> permissionMapper.selectPermissions(condition, level));
    }

    @Override
    public List<Permission> selectByCodes(String[] codes) {
        return selectByCondition(Condition.builder(Permission.class)
                .andWhere(Sqls.custom().andIn(Permission.FIELD_CODE, Arrays.asList(codes)))
                .build()
        );
    }

    @Override
    public List<Permission> selectByIds(List<Long> ids) {
        return selectByCondition(Condition.builder(Permission.class)
                .andWhere(Sqls.custom().andIn(Permission.FIELD_ID, ids))
                .build()
        );
    }

    @Override
    public List<Lov> selectLovByCodes(String[] codes, Long tenantId) {
        if (ArrayUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        return permissionMapper.selectLovByCodes(Arrays.asList(codes), tenantId);
    }

    @Override
    public void cacheServicePermissions(String serviceName, boolean clearCache) {
        serviceName = StringUtils.lowerCase(serviceName);
        List<Permission> permissions = select(Permission.FIELD_SERVICE_NAME, serviceName);
        LOGGER.info("cache service permissions, serviceName={}, permissionsSize={}", serviceName, permissions.size());

        String finalServiceName = serviceName;

        Map<String, Map<String, String>> map = permissions.parallelStream().collect(
                Collectors.groupingBy(Permission::getMethod,
                        Collectors.toMap(p -> String.valueOf(p.getId()), p -> redisHelper.toJson(new PermissionCacheVO(p)))
                )
        );

        SafeRedisHelper.execute(HZeroService.Gateway.REDIS_DB, () -> {
            if (clearCache) {
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.GET.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.POST.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.PUT.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.DELETE.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.PATCH.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.HEAD.name()));
                redisHelper.delKey(Permission.generateKey(finalServiceName, HttpMethod.OPTIONS.name()));
            }

            map.forEach((method, list) -> redisHelper.hshPutAll(Permission.generateKey(finalServiceName, method), list));
        });
    }

    @Override
    public List<Permission> selectSimpleByService(String serviceName) {
        return permissionMapper.selectSimpleByService(serviceName);
    }

    @Override
    public PermissionVO queryPermissionByCode(String permissionCode, String level) {
        return permissionMapper.queryPermissionByCode(permissionCode, level);
    }

    @Override
    @ProcessLovValue
    public Page<PermissionVO> pageTenantApis(PermissionVO permissionVO, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> permissionMapper.selectTenantApis(permissionVO));
    }

    @Override
    @ProcessLovValue
    public Page<PermissionVO> pageApis(PermissionVO permissionVO, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> permissionMapper.selectApis(permissionVO));
    }

    @Override
    @ProcessLovValue
    public Page<PermissionVO> pageTenantAssignableApis(PermissionVO permissionVO, PageRequest pageRequest) {
        return PageHelper.doPageAndSort(pageRequest, () -> permissionMapper.selectTenantAssignableApis(permissionVO));
    }

    @Override
    public List<Permission> listByServiceName(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return Collections.emptyList();
        }

        return this.selectByCondition(Condition.builder(Permission.class)
                .where(Sqls.custom()
                        .andEqualTo(Permission.FIELD_SERVICE_NAME, serviceName)
                ).build());
    }
}
