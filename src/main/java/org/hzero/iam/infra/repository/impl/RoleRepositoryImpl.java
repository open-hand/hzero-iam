package org.hzero.iam.infra.repository.impl;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.util.AsyncTask;
import org.hzero.core.util.CommonExecutor;
import org.hzero.iam.api.dto.*;
import org.hzero.iam.domain.entity.Menu;
import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.domain.entity.RolePermission;
import org.hzero.iam.domain.entity.User;
import org.hzero.iam.domain.repository.HiamProfileRepository;
import org.hzero.iam.domain.repository.MenuRepository;
import org.hzero.iam.domain.repository.RoleRepository;
import org.hzero.iam.domain.service.RootUserService;
import org.hzero.iam.domain.vo.ProfileVO;
import org.hzero.iam.domain.vo.RoleVO;
import org.hzero.iam.infra.common.utils.HiamMenuUtils;
import org.hzero.iam.infra.common.utils.HiamRoleUtils;
import org.hzero.iam.infra.common.utils.UserUtils;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.iam.infra.constant.HiamMemberType;
import org.hzero.iam.infra.mapper.RoleMapper;
import org.hzero.iam.infra.mapper.RolePermissionMapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.hzero.mybatis.common.Criteria;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.hzero.mybatis.util.Sqls;

/**
 * 角色资源库实现
 *
 * @author jiangzhou.bo@hand-china.com 2018/06/20 11:32
 */
@Component
public class RoleRepositoryImpl extends BaseRepositoryImpl<Role> implements RoleRepository {
    public static final Logger LOGGER = LoggerFactory.getLogger(RoleRepositoryImpl.class);

    private static final RoleVO EMPTY_ROLE = new RoleVO();

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private HiamProfileRepository profileRepository;
    @Autowired
    @Qualifier("IamCommonAsyncTaskExecutor")
    private ThreadPoolExecutor executor;

    @ProcessLovValue
    @ProcessCacheValue
    @Override
    public Page<RoleVO> selectSelfManageableRoles(RoleVO params, PageRequest pageRequest) {
        CustomUserDetails self = UserUtils.getUserDetails();

        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());

        RoleVO finalParams = params;
        Page<RoleVO> roleVos = PageHelper.doPage(pageRequest, () -> roleMapper.selectUserManageableRoles(finalParams));
        CustomUserDetails userDetails = UserUtils.getUserDetails();

        // 设置对应角色是否可被继承
        setupInheritable(roleVos.getContent(), userDetails.getTenantId());

        return roleVos;
    }

    private void setupInheritable(List<RoleVO> roleVos, Long tenantId) {
        List<AsyncTask<Boolean>> tasks = roleVos.stream().map(roleVo -> (AsyncTask<Boolean>) () -> {
            List<ProfileVO> profileVos = profileRepository.queryProfileVO(tenantId,
                    Constants.Config.CONFIG_CODE_ROLE_DISABLE_INHERIT);
            roleVo.setInheritable(Constants.DisplayStatus.ENABLE);
            for (ProfileVO profileVo : profileVos) {
                // 修复path为null时查询报错问题
                if (inheritDisabled(roleVo, profileVo) || createDisabled(roleVo, profileVo)) {
                    // 表示是继承关系，禁用
                    roleVo.setInheritable(Constants.DisplayStatus.DISABLED);
                    break;
                }
            }
            return Boolean.TRUE;
        }).collect(Collectors.toList());

        CommonExecutor.batchExecuteAsync(tasks, executor, "SetupInheritRole");
    }

    /**
     * 继承层级路径被包含在禁用路径中
     *
     * @param roleVo    角色
     * @param profileVo 禁用配置
     * @return true:禁用
     */
    private boolean inheritDisabled(RoleVO roleVo, ProfileVO profileVo) {
        return profileVo.getInheritLevelPath() != null && roleVo.getInheritLevelPath() != null
                && roleVo.getInheritLevelPath().contains(profileVo.getInheritLevelPath());
    }

    /**
     * h_level_path层级路径包含在禁用配置中
     *
     * @param roleVO    角色
     * @param profileVO 禁用配置
     * @return true:禁用
     */
    private boolean createDisabled(RoleVO roleVO, ProfileVO profileVO) {
        return profileVO.getLevelPath() != null && roleVO.getLevelPath() != null
                && roleVO.getLevelPath().contains(profileVO.getLevelPath());
    }


    @Override
    public List<RoleVO> selectSelfAllManageableRoles(RoleVO params) {
        CustomUserDetails self = UserUtils.getUserDetails();

        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());
        params.setSelectAssignedRoleFlag(true);

        SecurityTokenHelper.close();
        List<RoleVO> roles = roleMapper.selectUserManageableRoles(params);
        SecurityTokenHelper.clear();
        return roles;
    }

    @Override
    public List<RoleVO> selectUserManageableRoles(RoleVO params, User user) {
        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(user.getId());
        params.setUserTenantId(user.getOrganizationId());
        params.setUserOrganizationId(user.getOrganizationId());
        params.setSelectAssignedRoleFlag(true);

        SecurityTokenHelper.close();
        List<RoleVO> roles = roleMapper.selectUserManageableRoles(params);
        SecurityTokenHelper.clear();
        return roles;
    }

    @Override
    public Page<RoleVO> selectSelfAssignableRoles(RoleVO params, PageRequest pageRequest) {
        CustomUserDetails self = UserUtils.getUserDetails();

        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());
        params.setEnabled(true);
        // 排除用户已分配的角色
        if (CollectionUtils.isNotEmpty(params.getExcludeUserIds())) {
            Long excludeUserId = params.getExcludeUserIds().get(0);
            params.setExcludeUserId(excludeUserId);
        }
        params.setSelectAssignedRoleFlag(true);

        RoleVO finalParams = params;
        return PageHelper.doPage(pageRequest, () -> roleMapper.selectUserManageableRoles(finalParams));
    }

    @Override
    @ProcessLovValue
    public RoleVO selectRoleDetails(Long roleId) {
        return roleMapper.selectRoleDetails(roleId);
    }

    @Override
    public List<RoleVO> selectUserAdminRoles(RoleVO params, PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, () -> selectUserAdminRoles(params));
    }

    @Override
    @ProcessLovValue
    public Page<RoleVO> selectSelfAssignedRoles(RoleVO params, PageRequest pageRequest) {
        CustomUserDetails self = UserUtils.getUserDetails();

        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());

        RoleVO finalParams = params;
        return PageHelper.doPage(pageRequest, () -> roleMapper.selectUserAssignedRoles(finalParams));
    }

    @Override
    public List<RoleVO> selectUserAdminRoles(RoleVO params) {
        if (params.getUserId() == null) {
            CustomUserDetails self = UserUtils.getUserDetails();
            params.setUserId(self.getUserId());
        }

        return roleMapper.selectUserAdminRoles(params);
    }

    @Override
    public List<RoleVO> selectSelfCurrentTenantRoles(@Nullable Boolean notMerge) {
        CustomUserDetails self = UserUtils.getUserDetails();

        List<RoleVO> selfRoles = this.selectCurrentTenantMemberRoles(self, false);

        if (notMerge != null && notMerge) {
            return selfRoles;
        }

        List<RoleVO> returnRoles = new ArrayList<>(selfRoles.size());

        if (self.isRoleMergeFlag()) {
            Map<String, List<RoleVO>> map = selfRoles.stream().collect(Collectors.groupingBy(RoleVO::getLevel));
            // 如果只有一个层级的角色 则不返回角色列表，否则按层级分组，每个层级只返回一个角色
            if (map.size() > 1) {
                map.forEach((level, roles) -> {
                    RoleVO role = roles.get(0);
                    role.setName(RoleVO.obtainRoleName(role.getLevel(), role.getName(), self.getLanguage()));
                    returnRoles.add(role);
                });
            }
        } else {
            returnRoles.addAll(selfRoles);
        }

        return returnRoles;
    }

    @Override
    public RoleVO selectCurrentRole() {
        CustomUserDetails details = UserUtils.getUserDetails();
        if (details.getRoleId() == null) {
            return EMPTY_ROLE;
        }

        List<RoleVO> roles = this.selectCurrentTenantMemberRoles(details, true);
        return roles.stream().filter(r -> Objects.equals(r.getId(), details.getRoleId())).findFirst().orElse(EMPTY_ROLE);
    }

    @Override
    @ProcessLovValue
    public Page<RoleVO> selectSimpleRolesWithTenant(RoleVO params, PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, () -> roleMapper.selectSimpleRoles(params));
    }

    @Override
    public List<Role> selectSimpleRolesWithTenant(RoleVO params) {
        return roleMapper.selectSimpleRoles(params);
    }

    @Override
    public List<Menu> selectRolePermissionSetTree(Long roleId, PermissionSetSearchDTO permissionSetParam) {
        Role role = selectRoleSimpleById(roleId);
        Assert.notNull(role, "role is invalid");

        // 设置待分配角色ID
        permissionSetParam.setAllocateRoleId(roleId);

        // 当前角色取角色的父级角色
        Long currentRoleId = role.getParentRoleId();

        // 查询权限集
        List<Menu> menuList = menuRepository.selectRolePermissionSet(currentRoleId, roleId, permissionSetParam);

        // 格式化菜单树
        return HiamMenuUtils.formatMenuListToTree(menuList, Boolean.TRUE);
    }

    @Override
    public List<Role> selectParentRoles(Long roleId) {
        return roleMapper.selectAllParentRoles(roleId);
    }

    @Override
    public List<Role> selectAllSubRoles(Long roleId) {
        return roleMapper.selectAllSubRoles(roleId);
    }

    @Override
    public Role selectRoleSimpleById(Long roleId) {
        return selectOneOptional(new Role().setId(roleId),
                new Criteria().select(Role.FIELD_ID, Role.FIELD_CODE, Role.FIELD_NAME, Role.FIELD_LEVEL, Role.FIELD_PARENT_ROLE_ID,
                        Role.FIELD_IS_ENABLED, Role.FIELD_TENANT_ID, Role.FIELD_BUILD_IN)
                        .where(Role.FIELD_ID));
    }

    @Override
    public Role selectRoleSimpleByCode(String roleCode) {
        List<Role> roles = selectOptional(new Role().setCode(roleCode),
                new Criteria().select(Role.FIELD_ID, Role.FIELD_CODE, Role.FIELD_NAME, Role.FIELD_TENANT_ID, Role.FIELD_LEVEL)
                        .where(Role.FIELD_CODE));
        if (CollectionUtils.isEmpty(roles)) {
            return null;
        }
        if (roles.size() > 1) {
            throw new CommonException("hiam.warn.overOneRoleByCode");
        }
        return roles.get(0);
    }

    @Override
    public Role selectOneRoleByCode(String roleCode) {
        List<Role> list = select(Role.FIELD_CODE, roleCode);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.size() > 1) {
            throw new CommonException("hiam.warn.overOneRoleByCode");
        }
        return list.get(0);
    }

    @Override
    public Role selectRoleSimpleByLevelPath(String levelPath) {
        return selectOneOptional(new Role().setLevelPath(levelPath),
                new Criteria().select(Role.FIELD_ID, Role.FIELD_CODE, Role.FIELD_TENANT_ID, Role.FIELD_IS_ENABLED, Role.FIELD_LEVEL)
                        .where(Role.FIELD_LEVEL_PATH));
    }

    @Override
    public List<Role> selectBuiltInRoles(boolean includeSuperAdmin) {
        if (includeSuperAdmin) {
            return selectByCondition(Condition.builder(Role.class)
                    .andWhere(Sqls.custom().andEqualTo(Role.FIELD_BUILD_IN, BaseConstants.Flag.YES)).build());
        } else {
            return selectByCondition(Condition.builder(Role.class)
                    .andWhere(Sqls.custom().andEqualTo(Role.FIELD_BUILD_IN, BaseConstants.Flag.YES)
                            .andNotEqualTo(Role.FIELD_PARENT_ROLE_ID, Role.ROOT_ID))
                    .build());
        }
    }

    @Override
    @ProcessLovValue
    public Page<RoleVO> selectMemberRoles(Long memberId, HiamMemberType memberType, MemberRoleSearchDTO memberRoleSearchDTO, PageRequest pageRequest) {
        Page<RoleVO> page = PageHelper.doPage(pageRequest, () -> listMemberRoles(memberId, memberType, memberRoleSearchDTO));

        List<RoleVO> selfAllRoles = selectSelfAllManageableRoles(null);
        Set<Long> allRoleIds = selfAllRoles.stream().map(RoleVO::getId).collect(Collectors.toSet());

        for (RoleVO role : page.getContent()) {
            if (allRoleIds.contains(role.getId())) {
                role.setManageableFlag(BaseConstants.Flag.YES);
            }
        }

        return page;
    }

    @Override
    public List<RoleVO> listMemberRoles(Long memberId, HiamMemberType memberType, MemberRoleSearchDTO memberRoleSearchDTO) {
        RoleVO params = new RoleVO();
        params.setMemberId(memberId);
        params.setMemberType(memberType.value());
        params.setName(memberRoleSearchDTO.getRoleName());
        params.setTenantId(memberRoleSearchDTO.getTenantId());

        return roleMapper.selectMemberRoles(params);
    }

    @Override
    public List<Role> selectInheritSubRoleTreeWithPermissionSets(Long inheritRoleId, Set<Long> permissionSetIds, String type) {
        if (permissionSetIds.size() == 0 || inheritRoleId == null) {
            return Collections.emptyList();
        }

        // 由于一次性把所有角色权限查出来会很慢，所以拆分多个任务并行查询

        // 先查询继承体系的子角色
        List<Role> inheritedRoles = roleMapper.selectAllInheritedRole(inheritRoleId);

        batchSelectRolePermission(inheritedRoles, permissionSetIds, type);

        return HiamRoleUtils.formatRoleListToTree(inheritedRoles, true);
    }

    @Override
    public List<Role> selectCreatedSubRoleTreeWithPermissionSets(Long parentRoleId, Set<Long> permissionSetIds, String type) {
        if (permissionSetIds.size() == 0 || parentRoleId == null) {
            return Collections.emptyList();
        }

        // 由于一次性把所有角色权限查出来会很慢，所以拆分多个任务并行查询

        // 先查询创建体系的子角色
        List<Role> createdRoles = roleMapper.selectAllCreatedRole(parentRoleId);

        batchSelectRolePermission(createdRoles, permissionSetIds, type);

        return HiamRoleUtils.formatRoleListToTree(createdRoles, false);
    }

    private void batchSelectRolePermission(List<Role> roles, Set<Long> permissionSetIds, String type) {
        // 按每次最多查3000条数据计算，一次最多查 perMaxRole 个角色
        int perMaxRole = 5000 / permissionSetIds.size();

        Stream<RolePermission> rolePermissionStream;

        if (roles.size() <= perMaxRole) {
            List<RolePermission> resultList = rolePermissionMapper.selectRolePermissionSets(buildQueryParam(roles, permissionSetIds, type));
            rolePermissionStream = resultList.stream();
        } else {
            // 分成多组角色来查询
            List<List<Role>> partList = Lists.partition(roles, perMaxRole);
            // 构建批量查询任务
            List<AsyncTask<List<RolePermission>>> tasks = partList.stream()
                    .map(subRoleList -> (AsyncTask<List<RolePermission>>) () -> {
                        return rolePermissionMapper.selectRolePermissionSets(buildQueryParam(subRoleList, permissionSetIds, type));
                    })
                    .collect(Collectors.toList());
            // 执行批量查询任务
            List<List<RolePermission>> resultList = CommonExecutor.batchExecuteAsync(tasks, executor, "BatchSelectRolePermission");
            // 转成流
            rolePermissionStream = resultList.stream().flatMap(List::stream);
        }
        // 将权限集分发到各个角色
        Map<Long, Role> mapRole = roles.stream().collect(Collectors.toMap(Role::getId, Function.identity()));

        Map<Long, List<RolePermission>> mapRps = rolePermissionStream.parallel().collect(Collectors.groupingBy(RolePermission::getRoleId, Collectors.toList()));

        mapRole.forEach((id, role) -> {
            role.setPermissionSets(Optional.ofNullable(mapRps.get(id)).orElse(new ArrayList<>()));
        });
    }

    private RolePermission buildQueryParam(List<Role> roles, Set<Long> permissionSetIds, String type) {
        RolePermission params = new RolePermission();
        params.setPermissionSetIds(permissionSetIds);
        params.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toSet()));
        params.setType(type);
        return params;
    }

    @Override
    public RoleVO selectAdminRole(Long roleId) {
        CustomUserDetails self = UserUtils.getUserDetails();
        RoleVO params = new RoleVO();
        params.setId(roleId);
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());
        return roleMapper.selectAdminRole(params);
    }

    @Override
    public List<RolePermission> selectRolePermissions(RolePermission params) {
        return rolePermissionMapper.selectRolePermissionSets(params);
    }

    @Override
    public List<Role> listTenantAdmin(Long tenantId) {
        return roleMapper.selectTenantAdmin(tenantId);
    }

    @ProcessLovValue
    @ProcessCacheValue
    @Override
    public Page<RoleDTO> selectUserManageableRoleTree(PageRequest pageRequest, RoleVO params) {
        CustomUserDetails self = UserUtils.getUserDetails();
        params = Optional.ofNullable(params).orElse(new RoleVO());
        params.setUserId(self.getUserId());
        params.setUserTenantId(self.getTenantId());
        params.setUserOrganizationId(self.getOrganizationId());

        // null 认为查根节点list,配合前端
        if (Objects.isNull(params.getParentRoleId())) {
            params.setQueryRootNodeFlag(1);
            params.setParentRoleId(null);
        }

        // 分页查询
        RoleVO finalParams = params;
        Page<RoleVO> page = PageHelper.doPageAndSort(pageRequest,
                () -> roleMapper.selectUserManageableRoleTree(finalParams));

        // 检查是否有孩子,考虑到是分页查询，数据量可控，这里我们for循环查是否有孩子，前端组件解析需要
        List<RoleDTO> roleDTOList = page.getContent().stream().map(item -> {
            RoleDTO roleDTO = new RoleDTO();
            BeanUtils.copyProperties(item, roleDTO);
            if (roleDTO.getChildrenNum() != null && roleDTO.getChildrenNum() > 0) {
                // 前端判断用
                roleDTO.addChildren(Collections.emptyList());
            }
            return roleDTO;
        }).collect(Collectors.toList());

        // 封装返回数据
        Page<RoleDTO> roleDTOPage = new Page<>();
        BeanUtils.copyProperties(page, roleDTOPage);
        roleDTOPage.setContent(roleDTOList);
        return roleDTOPage;
    }

    @Override
    public List<Role> selectUserRole(Long tenantId, Long userId) {
        return roleMapper.selectUserRole(tenantId, userId);
    }

    @ProcessLovValue
    @ProcessCacheValue
    @Override
    public List<RoleVO> selectUserManageableRoleTree(RoleVO params) {
        return roleMapper.selectUserManageableRoleTree(params);
    }

    @Override
    public boolean checkPermission(RolePermissionCheckDTO rolePermissionCheckDTO) {
        Assert.notNull(rolePermissionCheckDTO, "parameter cannot be null.");
        rolePermissionCheckDTO.validate();
        return rolePermissionMapper.checkPermission(rolePermissionCheckDTO) > 0;
    }

    /**
     * 查询角色的所有子集包含自己
     *
     * @param roleId 角色ID
     * @return 角色子集
     */
    @Override
    public List<Role> selectAllSubRolesIncludeSelf(Long roleId) {

        List<Role> roles = roleMapper.selectAllSubRolesIncludeSelf(roleId);
        return HiamRoleUtils.formatRoleListToTree(roles, false);
    }

    @Override
    public Page<RoleVO> selectSecGrpAssignableRole(Long secGrpId, Long roleId, RoleSecGrpDTO queryDTO, PageRequest pageRequest) {
        // 分页查询数据，并返回结果
        return PageHelper.doPage(pageRequest, () -> this.roleMapper.selectSecGrpAssignableRole(secGrpId, roleId, queryDTO));
    }

    @Override
    public Page<RoleVO> selectByRoleIds(List<Long> roleIds, PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, () -> roleMapper.selectByRoleIds(roleIds));
    }

    @Override
    public List<Role> selectBuiltInTemplateRole(String roleLabel) {
        return roleMapper.selectBuiltInTemplateRole(roleLabel);
    }

    @Override
    public Map<String, String> selectTplRoleNameById(Long roleId) {
        List<Map<String, String>> langs = roleMapper.selectTplRoleNameById(roleId);
        return langs.stream()
                .filter(m -> StringUtils.isNoneBlank(m.get("lang"), m.get("name")))
                .collect(Collectors.toMap(m -> m.get("lang"), m -> m.get("name")));
    }

    @Override
    public Long countSubRole(Long parentRoleId, Long roleId) {
        return roleMapper.countSubRole(parentRoleId, roleId);
    }

    @Override
    public void batchUpdateEnableFlag(Long roleId, Integer enableFlag, boolean updateSubRole) {
        roleMapper.batchUpdateEnableFlagBySql(roleId, enableFlag, updateSubRole);
    }

    @Override
    public List<RoleVO> selectSubAssignedRoles(Long roleId, Long userId) {
        return roleMapper.selectSubAssignedRoles(roleId, userId);
    }

    @Override
    public List<Role> selectRoleByLabel(Long tenantId, @Nonnull Set<String> roleLabels, String assignType) {
        if (CollectionUtils.isEmpty(roleLabels)) {
            return Collections.emptyList();
        }
        return roleMapper.selectRoleByLabel(tenantId, roleLabels, assignType);
    }

    @Override
    public boolean isTopAdminRole(Long userId, Long roleId) {
        List<Long> topRoleIds = roleMapper.queryTopAdminRoleId(userId, roleId);
        return topRoleIds != null && topRoleIds.contains(roleId);
    }

    private List<RoleVO> selectCurrentTenantMemberRoles(CustomUserDetails self, boolean onlyCurrentRole) {
        RoleVO params = new RoleVO();
        params.setMemberId(self.getUserId());
        params.setMemberType(HiamMemberType.USER.value());
        params.setTenantId(self.getTenantId());
        params.setCheckMemberRoleExpire(true);
        params.setQueryAdminFlag(true);

        if (onlyCurrentRole) {
            params.setId(self.getRoleId());
        }

        List<RoleVO> roles = roleMapper.selectMemberRoles(params);

        if (RootUserService.isRootUser()) {
            List<RoleVO> rootRoles = roleMapper.selectRootMemberRoles(params);
            Set<Long> ids = roles.stream().map(RoleVO::getId).collect(Collectors.toSet());
            rootRoles = rootRoles.stream().filter(r -> !ids.contains(r.getId())).collect(Collectors.toList());
            roles.addAll(rootRoles);
        }
        return roles;
    }
}
