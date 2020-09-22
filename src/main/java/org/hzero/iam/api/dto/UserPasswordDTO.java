package org.hzero.iam.api.dto;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author superlee
 * @since 2018/4/12
 */
public class UserPasswordDTO {

    @ApiModelProperty(value = "新密码/必填")
    @NotEmpty
    private String password;

    @ApiModelProperty(value = "原始密码/必填")
    @NotEmpty
    private String originalPassword;
    @ApiModelProperty(value = "组织Id/必填")
    private Long organizationId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOriginalPassword() {
        return originalPassword;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }
}
