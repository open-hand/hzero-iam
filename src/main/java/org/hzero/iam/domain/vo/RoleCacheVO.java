package org.hzero.iam.domain.vo;

import java.io.Serializable;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 角色缓存VO
 *
 * @author bojiangzhou 2019/04/16
 */
public class RoleCacheVO implements Serializable {
    private static final long serialVersionUID = -4929785401176780474L;

    @Encrypt
    private Long id;
    private String name;
    private String code;
    private String description;
    private String level;
    private Boolean isEnabled;
    private Boolean isBuiltIn;
    private Long tenantId;
    @Encrypt
    private Long inheritRoleId;
    @Encrypt
    private Long parentRoleId;
    private String parentRoleAssignLevel;
    private Long parentRoleAssignLevelValue;
    private String viewCode;

    public RoleCacheVO() {
    }

    public RoleCacheVO(RoleVO role) {
        this.id = role.getId();
        this.name = role.getName();
        this.code = role.getCode();
        this.description = role.getDescription();
        this.level = role.getLevel();
        this.tenantId = role.getTenantId();
        this.inheritRoleId = role.getInheritRoleId();
        this.parentRoleId = role.getParentRoleId();
        this.parentRoleAssignLevel = role.getParentRoleAssignLevel();
        this.parentRoleAssignLevelValue = role.getParentRoleAssignLevelValue();
        this.viewCode = role.getViewCode();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getBuiltIn() {
        return isBuiltIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        isBuiltIn = builtIn;
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

    public void setParentRoleId(Long parentRoleId) {
        this.parentRoleId = parentRoleId;
    }

    public String getParentRoleAssignLevel() {
        return parentRoleAssignLevel;
    }

    public void setParentRoleAssignLevel(String parentRoleAssignLevel) {
        this.parentRoleAssignLevel = parentRoleAssignLevel;
    }

    public Long getParentRoleAssignLevelValue() {
        return parentRoleAssignLevelValue;
    }

    public void setParentRoleAssignLevelValue(Long parentRoleAssignLevelValue) {
        this.parentRoleAssignLevelValue = parentRoleAssignLevelValue;
    }

    public String getViewCode() {
        return viewCode;
    }

    public void setViewCode(String viewCode) {
        this.viewCode = viewCode;
    }
}
