package org.hzero.iam.infra.repository.impl;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

import org.hzero.core.base.BaseConstants.Symbol;
import org.hzero.core.util.AsyncTask;
import org.hzero.core.util.CommonExecutor;
import org.hzero.iam.api.dto.RolePermissionWithDTO;
import org.hzero.iam.domain.entity.RolePermission;
import org.hzero.iam.domain.repository.RolePermissionRepository;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.iam.infra.constant.Operation;
import org.hzero.iam.infra.mapper.RolePermissionMapper;
import org.hzero.iam.infra.util.BatchSqlHelper;
import org.hzero.iam.infra.util.CollectionSubUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 角色权限(集)资源库
 *
 * @author jiangzhou.bo@hand-china.com 2018/06/20 20:51
 */
@Component
public class RolePermissionRepositoryImpl extends BaseRepositoryImpl<RolePermission> implements RolePermissionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RolePermissionRepositoryImpl.class);

    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    @Qualifier("IamCommonAsyncTaskExecutor")
    private ThreadPoolExecutor executor;

    @Override
    public List<RolePermission> selectRolePermissionSets(RolePermission params) {
        // 设置默认类型为权限集类型，必输项
        if (StringUtils.isBlank(params.getType())) {
            params.setType(RolePermission.DEFAULT_TYPE.name());
        }
        return rolePermissionMapper.selectRolePermissionSets(params);
    }

    @Override
    public List<RolePermissionWithDTO> selectRoleWithPermission(List<Long> roleIds, long permissionSetId, String menuType) {
        return rolePermissionMapper.selectRoleWithPermission(roleIds, permissionSetId, menuType);
    }

    @Override
    public void batchInsertBySql(List<RolePermission> permissionSets) {
        if (CollectionUtils.isEmpty(permissionSets)) {
            return;
        }

        CustomUserDetails details = DetailsHelper.getUserDetails();
        if (details != null) {
            Long userId = details.getUserId();
            permissionSets.forEach(item -> {
                item.setCreatedBy(userId);
                item.setLastUpdatedBy(userId);
            });
        }

        BatchSqlHelper.batchExecute(permissionSets, 8,
                (dataList) -> rolePermissionMapper.batchInsertBySql(dataList),
                "BatchInsertRolePermission");
    }

    @Override
    public void batchDeleteBySql(List<RolePermission> permissionSets) {
        if (CollectionUtils.isEmpty(permissionSets)) {
            return;
        }

        Set<Long> ids = getPrimaryIds(permissionSets);

        BatchSqlHelper.batchExecute(ids, 2,
                (dataList) -> rolePermissionMapper.batchDeleteBySql(dataList),
                "BatchDeleteRolePermission");
    }

    @Override
    public void batchUpdateBySql(List<RolePermission> permissionSets) {
        // 按 createFlag + inheritFlag 分组
        Map<String, List<RolePermission>> flagMap = permissionSets.stream()
                .collect(Collectors.groupingBy(rp -> rp.getCreateFlag() + Symbol.MIDDLE_LINE + rp.getInheritFlag(), Collectors.toList()));

        // 批量更新
        List<AsyncTask<Integer>> tasks = new ArrayList<>();
        flagMap.forEach((key, value) -> {
            String[] arr = key.split(Symbol.MIDDLE_LINE);
            LOGGER.debug("Batch update RolePermission, flagKey: {}, updateSize: {}", key, value.size());

            List<List<RolePermission>> subList = CollectionSubUtils.subList(value, Constants.BATCH_SIZE);

            List<AsyncTask<Integer>> subTasks = subList.stream().map(list -> (AsyncTask<Integer>) () -> {
                rolePermissionMapper.batchUpdateBySql(getPrimaryIds(list), arr[0], arr[1]);
                return list.size();
            }).collect(Collectors.toList());

            tasks.addAll(subTasks);
        });

        CommonExecutor.batchExecuteAsync(tasks, executor, "BatchUpdateRolePermission");
    }

    @Override
    public Map<Operation, List<RolePermission>> batchSaveRolePermission(Stream<RolePermission> stream) {
        Map<Operation, List<RolePermission>> map =
                stream.peek(rp -> {
                    if (rp.getOperation() == null) {
                        rp.setOperation(Operation.NONE);
                    }
                })
                .collect(Collectors.groupingBy(RolePermission::getOperation, Collectors.toList()));

        map.putIfAbsent(Operation.INSERT, Collections.emptyList());
        map.putIfAbsent(Operation.DELETE, Collections.emptyList());
        map.putIfAbsent(Operation.UPDATE, Collections.emptyList());
        map.putIfAbsent(Operation.NONE, Collections.emptyList());

        List<RolePermission> insertList = map.get(Operation.INSERT);
        List<RolePermission> deleteList = map.get(Operation.DELETE);
        List<RolePermission> updateList = map.get(Operation.UPDATE);


        long start = System.currentTimeMillis();

        // 批量插入
        this.batchInsertBySql(insertList);
        // 批量删除
        this.batchDeleteBySql(deleteList);
        // 批量更新
        this.batchUpdateBySql(updateList);

        long minus = System.currentTimeMillis() - start;

        LOGGER.info("Batch save RolePermission, insertSize: {}, deleteSize: {}, updateSize: {}, costTime: {}",
                insertList.size(), deleteList.size(), updateList.size(), minus);

        return map;
    }

    private Set<Long> getPrimaryIds(List<RolePermission> rolePermissions) {
        return rolePermissions.stream().map(RolePermission::getId).collect(Collectors.toSet());
    }

}
