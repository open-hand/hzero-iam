package org.hzero.iam.domain.vo;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.format.annotation.DateTimeFormat;

import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;
import org.hzero.core.message.MessageAccessor;
import org.hzero.iam.domain.entity.LabelRel;
import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.infra.constant.HiamResourceLevel;
import org.hzero.mybatis.domian.SecurityToken;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 角色VO
 *
 * @author jiangzhou.bo@hand-china.com 2018/06/21 15:32
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleVO implements SecurityToken, Cacheable {

    private static final Map<String, String> ROLE_NAME;

    static {
        Map<String, String> roleName = new HashMap<>(2);
        roleName.put(HiamResourceLevel.SITE.value(), "hiam.info.role.siteName");
        roleName.put(HiamResourceLevel.ORGANIZATION.value(), "hiam.info.role.tenantName");
        ROLE_NAME = Collections.unmodifiableMap(roleName);
    }

    @Encrypt
    private Long id;
    private String name;
    private String code;
    private String description;
    @LovValue("HIAM.RESOURCE_LEVEL")
    private String level;
    private Boolean isEnabled;
    private Boolean isModified;
    private Boolean isEnableForbidden;
    private Boolean isBuiltIn;
    private Boolean isAssignable;
    private Long tenantId;
    @Encrypt
    private Long inheritRoleId;
    @Encrypt
    private Long parentRoleId;
    private String parentRoleAssignLevel;
    private Long parentRoleAssignLevelValue;
    private Long createdBy;
    private Date creationDate;
    private Long objectVersionNumber;
    private String inheritLevelPath;
    private String viewCode;
    private String levelMeaning;
    private String inheritedRoleName;
    private String parentRoleName;
    private String tenantNum;
    private String tenantName;
    @LovValue("HIAM.ROLE_SOURCE")
    private String roleSource;
    private Long defaultRoleId;
    private LocalDate startDateActive;
    private LocalDate endDateActive;
    /**
     * 角色标签集合
     */
    private List<LabelRel> roleLabels;
    /**
     * 是否为默认角色
     */
    private Boolean isDefaultRole;
    /**
     * 是否显示继承按钮(1/0)
     */
    private Long inheritable = 1L;
    /**
     * 创建者名称
     */
    @CacheValue(
        key = HZeroCacheKey.USER,
        primaryKey = "createdBy",
        searchKey = "realName",
        structure = CacheValue.DataStructure.MAP_OBJECT
    )
    private String createdUserName;
    //
    // member_role 相关
    // ------------------------------------------------------------------------------
    @Encrypt
    private Long memberId;
    private String memberType;
    private Long sourceId;
    private String sourceType;
    @LovValue("HIAM.RESOURCE_LEVEL")
    private String assignLevel;
    private String assignLevelMeaning;
    private Long assignLevelValue;
    private String assignLevelValueMeaning;
    private String roleSourceMeaning;
    @JsonIgnore
    private List<PermissionVO> permissions;
    @JsonIgnore
    private RoleVO inheritedRole;
    /**
     * 只查第一层节点节点标识
     */
    private Integer queryRootNodeFlag;
    /**
     * 管理标识(可以创建、修改角色)
     */
    @LovValue("HPFM.FLAG")
    private Integer adminFlag = 0;

    private String adminFlagMeaning;
    /**
     * 分配标识
     */
    private Integer assignedFlag  = 0;

    //
    // 管理角色相关
    // ------------------------------------------------------------------------------
    /**
     * 是否有父级管理角色
     */
    private Integer haveAdminFlag = 0;
    /**
     * 是否超级管理员
     */
    private Integer superAdminFlag;
    /**
     * 路径
     */
    private String levelPath;
    //
    // 父级管理角色相关
    // ------------------------------------------------------------------------------
    @Encrypt
    private Long adminRoleId;
    private String adminRoleCode;
    private String adminRoleName;
    private String adminRoleLevel;
    private Long adminRoleTenantId;
    private String adminRoleTenantNum;
    private String adminRoleTenantName;
    // 父级角色分配组织ID
    private Long parentRoleAssignUnitId;
    // 父级角色分配组织名称
    private String parentRoleAssignUnitName;
    // 是否在管理范围内
    private Integer manageableFlag = 0;
    /**
     * 查询参数(角色的创建区间)：
     *  1. 开始时间
     *  2. 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createStartDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createEndDate;
    /**
     * 查询参数(角色的更新区间)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateStartDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateEndDate;
    /**
     * 当前用户
     */
    @JsonIgnore
    @Encrypt
    private Long userId;
    /**
     * 当前用户租户ID
     */
    @JsonIgnore
    private Long userTenantId;
    /**
     * 当前用户所属租户
     */
    @JsonIgnore
    private Long userOrganizationId;
    /**
     * 查询参数：是否检查成员角色过期
     */
    @JsonIgnore
    private boolean checkMemberRoleExpire = false;
    /**
     * 查询参数：是否查询管理标识
     */
    @JsonIgnore
    private boolean queryAdminFlag = false;

    /**
     * 查询参数:
     * 排除角色ID列表
     * 排除用户已分配角色, 用户ID列表
     */
    @Encrypt
    @JsonIgnore
    private List<Long> excludeRoleIds;
    @Encrypt
    @JsonIgnore
    private List<Long> excludeUserIds;
    // 排除的用户ID
    @Encrypt
    private Long excludeUserId;
    // 查询可分配的角色
    @JsonIgnore
    private boolean selectAssignedRoleFlag = false;
    // 孩子数量
    private Integer childrenNum;
    /**
     * 用于按照分配先后次序排序
     */
    private Long memberRoleId;
    /**
     * 排除的安全组ID
     */
    private Long excludeSecGrpId;

    @JsonIgnore
    private List<Long> queryRoleIds;
    /**
     * 查询条件：角色标签s
     */
    private Set<String> labels;
    private String _token;

    public RoleVO() {
    }

    public RoleVO(Long id) {
        this.id = id;
    }

    public static String obtainRoleName(String level, String defaultName, String lang) {
        return MessageAccessor.getMessage(RoleVO.ROLE_NAME.get(level), defaultName, LocaleUtils.toLocale(lang)).desc();
    }

    public Role toRole() {
        Role role = new Role();
        role.setId(this.id);
        role.setCode(this.code);
        role.setTenantId(this.tenantId);
        role.setParentRoleId(this.parentRoleId);
        role.setInheritRoleId(this.inheritRoleId);
        role.setLevel(this.level);
        role.setLevelPath(this.levelPath);
        role.setEnabled(this.isEnabled);
        role.setParentRoleAssignLevel(this.parentRoleAssignLevel);
        role.setParentRoleAssignLevelValue(this.parentRoleAssignLevelValue);
        return role;
    }

    public String getViewCode() {
        //if (StringUtils.isNotBlank(code)) {
        //    String[] codeArr = StringUtils.split(code, BaseConstants.Symbol.SLASH);
        //    this.viewCode = codeArr[codeArr.length-1];
        //}
        this.viewCode = this.code;
        // viewCode 展示整个编码
        return this.viewCode;
    }

    public void setViewCode(String viewCode) {
        this.viewCode = viewCode;
    }

    public List<LabelRel> getRoleLabels() {
        return roleLabels;
    }

    public void setRoleLabels(List<LabelRel> roleLabels) {
        this.roleLabels = roleLabels;
    }

    public Long getInheritable() {
        return inheritable;
    }

    public void setInheritable(Long inheritable) {
        this.inheritable = inheritable;
    }

    public Long getId() {
        return id;
    }

    public RoleVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Date getCreateStartDate() {
        return createStartDate;
    }

    public void setCreateStartDate(Date createStartDate) {
        this.createStartDate = createStartDate;
    }

    public Date getCreateEndDate() {
        return createEndDate;
    }

    public void setCreateEndDate(Date createEndDate) {
        this.createEndDate = createEndDate;
    }

    public Date getUpdateStartDate() {
        return updateStartDate;
    }

    public void setUpdateStartDate(Date updateStartDate) {
        this.updateStartDate = updateStartDate;
    }

    public Date getUpdateEndDate() {
        return updateEndDate;
    }

    public void setUpdateEndDate(Date updateEndDate) {
        this.updateEndDate = updateEndDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getEnabled() {
        return isEnabled != null ? isEnabled : false;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getModified() {
        return isModified;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
    }

    public Boolean getEnableForbidden() {
        return isEnableForbidden;
    }

    public void setEnableForbidden(Boolean enableForbidden) {
        isEnableForbidden = enableForbidden;
    }

    public Boolean getBuiltIn() {
        return isBuiltIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        isBuiltIn = builtIn;
    }

    public Boolean getAssignable() {
        return isAssignable;
    }

    public void setAssignable(Boolean assignable) {
        isAssignable = assignable;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getInheritRoleId() {
        return inheritRoleId;
    }

    public void setInheritRoleId(Long inheritRoleId) {
        this.inheritRoleId = inheritRoleId;
    }

    public Long getParentRoleId() {
        return parentRoleId;
    }

    public RoleVO setParentRoleId(Long parentRoleId) {
        this.parentRoleId = parentRoleId;
        return this;
    }

    public String getInheritLevelPath() {
        return inheritLevelPath;
    }

    public void setInheritLevelPath(String inheritLevelPath) {
        this.inheritLevelPath = inheritLevelPath;
    }

    public String getParentRoleAssignLevel() {
        return parentRoleAssignLevel;
    }

    public RoleVO setParentRoleAssignLevel(String parentRoleAssignLevel) {
        this.parentRoleAssignLevel = parentRoleAssignLevel;
        return this;
    }

    public Long getParentRoleAssignLevelValue() {
        return parentRoleAssignLevelValue;
    }

    public RoleVO setParentRoleAssignLevelValue(Long parentRoleAssignLevelValue) {
        this.parentRoleAssignLevelValue = parentRoleAssignLevelValue;
        return this;
    }

    public String getAssignLevelMeaning() {
        return assignLevelMeaning;
    }

    public void setAssignLevelMeaning(String assignLevelMeaning) {
        this.assignLevelMeaning = assignLevelMeaning;
    }

    public String getAssignLevelValueMeaning() {
        return assignLevelValueMeaning;
    }

    public void setAssignLevelValueMeaning(String assignLevelValueMeaning) {
        this.assignLevelValueMeaning = assignLevelValueMeaning;
    }

    public String getRoleSource() {
        return this.roleSource;
    }

    public void setRoleSource(String roleSource) {
        this.roleSource = roleSource;
    }

    /**
     * @return 层级含义
     */
    public String getLevelMeaning() {
        return levelMeaning;
    }

    public void setLevelMeaning(String levelMeaning) {
        this.levelMeaning = levelMeaning;
    }

    /**
     * @return 继承的角色名称
     */
    public String getInheritedRoleName() {
        return inheritedRoleName;
    }

    public void setInheritedRoleName(String inheritedRoleName) {
        this.inheritedRoleName = inheritedRoleName;
    }

    public Long getMemberId() {
        return memberId;
    }

    public RoleVO setMemberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getTenantNum() {
        return tenantNum;
    }

    public void setTenantNum(String tenantNum) {
        this.tenantNum = tenantNum;
    }

    /**
     * @return 租户名称
     */
    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getAssignLevel() {
        return assignLevel;
    }

    public void setAssignLevel(String assignLevel) {
        this.assignLevel = assignLevel;
    }

    public Long getAssignLevelValue() {
        return assignLevelValue;
    }

    public void setAssignLevelValue(Long assignLevelValue) {
        this.assignLevelValue = assignLevelValue;
    }

    public List<PermissionVO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionVO> permissions) {
        this.permissions = permissions;
    }

    /**
     * @return 继承的角色
     */
    public RoleVO getInheritedRole() {
        return inheritedRole;
    }

    public void setInheritedRole(RoleVO inheritedRole) {
        this.inheritedRole = inheritedRole;
    }

    public String getParentRoleName() {
        return parentRoleName;
    }

    public void setParentRoleName(String parentRoleName) {
        this.parentRoleName = parentRoleName;
    }

    public List<Long> getExcludeRoleIds() {
        return excludeRoleIds;
    }

    public void setExcludeRoleIds(List<Long> excludeRoleIds) {
        this.excludeRoleIds = excludeRoleIds;
    }

    public List<Long> getExcludeUserIds() {
        return excludeUserIds;
    }

    public void setExcludeUserIds(List<Long> excludeUserIds) {
        this.excludeUserIds = excludeUserIds;
    }

    public Long getExcludeUserId() {
        return excludeUserId;
    }

    public void setExcludeUserId(Long excludeUserId) {
        this.excludeUserId = excludeUserId;
    }

    public Long getMemberRoleId() {
        return memberRoleId;
    }

    public void setMemberRoleId(Long memberRoleId) {
        this.memberRoleId = memberRoleId;
    }

    public Long getDefaultRoleId() {
        return defaultRoleId;
    }

    public void setDefaultRoleId(Long defaultRoleId) {
        this.defaultRoleId = defaultRoleId;
    }

    public Boolean getDefaultRole() {
        return isDefaultRole;
    }

    public void setDefaultRole(Boolean defaultRole) {
        isDefaultRole = defaultRole;
    }

    public Integer getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(Integer adminFlag) {
        this.adminFlag = adminFlag;
    }

    public String getAdminFlagMeaning() {
        return adminFlagMeaning;
    }

    public void setAdminFlagMeaning(String adminFlagMeaning) {
        this.adminFlagMeaning = adminFlagMeaning;
    }

    public Integer getAssignedFlag() {
        return assignedFlag;
    }

    public void setAssignedFlag(Integer assignedFlag) {
        this.assignedFlag = assignedFlag;
    }

    public Integer getHaveAdminFlag() {
        return haveAdminFlag;
    }

    public void setHaveAdminFlag(Integer haveAdminFlag) {
        this.haveAdminFlag = haveAdminFlag;
    }

    public Integer getSuperAdminFlag() {
        return superAdminFlag;
    }

    public void setSuperAdminFlag(Integer superAdminFlag) {
        this.superAdminFlag = superAdminFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserTenantId() {
        return userTenantId;
    }

    public void setUserTenantId(Long userTenantId) {
        this.userTenantId = userTenantId;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
    }

    public boolean isCheckMemberRoleExpire() {
        return checkMemberRoleExpire;
    }

    public void setCheckMemberRoleExpire(boolean checkMemberRoleExpire) {
        this.checkMemberRoleExpire = checkMemberRoleExpire;
    }

    public String getRoleSourceMeaning() {
        return roleSourceMeaning;
    }

    public void setRoleSourceMeaning(String roleSourceMeaning) {
        this.roleSourceMeaning = roleSourceMeaning;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public Long getAdminRoleId() {
        return adminRoleId;
    }

    public void setAdminRoleId(Long adminRoleId) {
        this.adminRoleId = adminRoleId;
    }

    public String getAdminRoleCode() {
        return adminRoleCode;
    }

    public void setAdminRoleCode(String adminRoleCode) {
        this.adminRoleCode = adminRoleCode;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    public String getAdminRoleLevel() {
        return adminRoleLevel;
    }

    public void setAdminRoleLevel(String adminRoleLevel) {
        this.adminRoleLevel = adminRoleLevel;
    }

    public Long getAdminRoleTenantId() {
        return adminRoleTenantId;
    }

    public void setAdminRoleTenantId(Long adminRoleTenantId) {
        this.adminRoleTenantId = adminRoleTenantId;
    }

    public String getAdminRoleTenantNum() {
        return adminRoleTenantNum;
    }

    public void setAdminRoleTenantNum(String adminRoleTenantNum) {
        this.adminRoleTenantNum = adminRoleTenantNum;
    }

    public String getAdminRoleTenantName() {
        return adminRoleTenantName;
    }

    public void setAdminRoleTenantName(String adminRoleTenantName) {
        this.adminRoleTenantName = adminRoleTenantName;
    }

    public Long getParentRoleAssignUnitId() {
        return parentRoleAssignUnitId;
    }

    public void setParentRoleAssignUnitId(Long parentRoleAssignUnitId) {
        this.parentRoleAssignUnitId = parentRoleAssignUnitId;
    }

    public String getParentRoleAssignUnitName() {
        return parentRoleAssignUnitName;
    }

    public void setParentRoleAssignUnitName(String parentRoleAssignUnitName) {
        this.parentRoleAssignUnitName = parentRoleAssignUnitName;
    }

    public boolean isSelectAssignedRoleFlag() {
        return selectAssignedRoleFlag;
    }

    public void setSelectAssignedRoleFlag(boolean selectAssignedRoleFlag) {
        this.selectAssignedRoleFlag = selectAssignedRoleFlag;
    }

    public Integer getManageableFlag() {
        return manageableFlag;
    }

    public void setManageableFlag(Integer manageableFlag) {
        this.manageableFlag = manageableFlag;
    }

    @Override
    public String get_token() {
        return this._token;
    }

    @Override
    public void set_token(String _token) {
        this._token = _token;
    }

    @Override
    public Class<? extends SecurityToken> associateEntityClass() {
        return Role.class;
    }

    public Integer getQueryRootNodeFlag() {
        return queryRootNodeFlag;
    }

    public void setQueryRootNodeFlag(Integer queryRootNodeFlag) {
        this.queryRootNodeFlag = queryRootNodeFlag;
    }

    public String getLevelPath() {
        return levelPath;
    }

    public void setLevelPath(String levelPath) {
        this.levelPath = levelPath;
    }

    public Integer getChildrenNum() {
        return childrenNum;
    }

    public void setChildrenNum(Integer childrenNum) {
        this.childrenNum = childrenNum;
    }

    public Long getExcludeSecGrpId() {
        return excludeSecGrpId;
    }

    public void setExcludeSecGrpId(Long excludeSecGrpId) {
        this.excludeSecGrpId = excludeSecGrpId;
    }

    public LocalDate getStartDateActive() {
        return startDateActive;
    }

    public void setStartDateActive(LocalDate startDateActive) {
        this.startDateActive = startDateActive;
    }

    public LocalDate getEndDateActive() {
        return endDateActive;
    }

    public void setEndDateActive(LocalDate endDateActive) {
        this.endDateActive = endDateActive;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public boolean isQueryAdminFlag() {
        return queryAdminFlag;
    }

    public void setQueryAdminFlag(boolean queryAdminFlag) {
        this.queryAdminFlag = queryAdminFlag;
    }

    public List<Long> getQueryRoleIds() {
        return queryRoleIds;
    }

    public void setQueryRoleIds(List<Long> queryRoleIds) {
        this.queryRoleIds = queryRoleIds;
    }
}
