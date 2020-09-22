package org.hzero.iam.domain.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * 密码策略
 *
 * @author bojiangzhou 2019/08/05
 */
public class PasswordPolicyVO implements Serializable {
    private static final long serialVersionUID = 294437235538711407L;

    @ApiModelProperty(value = "最小密码长度/非必填")
    private Integer minLength;
    @ApiModelProperty(value = "最大密码长度/非必填")
    private Integer maxLength;
    @ApiModelProperty(value = "最少数字数/非必填")
    private Integer digitsCount;
    @ApiModelProperty(value = "最少小写字母数/非必填")
    private Integer lowercaseCount;
    @ApiModelProperty(value = "最少大写字母数/非必填")
    private Integer uppercaseCount;
    @ApiModelProperty(value = "最少特殊字符数/非必填")
    private Integer specialCharCount;
    @ApiModelProperty(value = "是否允许与登录名相同/非必填")
    private Boolean notUsername;

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getDigitsCount() {
        return digitsCount;
    }

    public void setDigitsCount(Integer digitsCount) {
        this.digitsCount = digitsCount;
    }

    public Integer getLowercaseCount() {
        return lowercaseCount;
    }

    public void setLowercaseCount(Integer lowercaseCount) {
        this.lowercaseCount = lowercaseCount;
    }

    public Integer getUppercaseCount() {
        return uppercaseCount;
    }

    public void setUppercaseCount(Integer uppercaseCount) {
        this.uppercaseCount = uppercaseCount;
    }

    public Integer getSpecialCharCount() {
        return specialCharCount;
    }

    public void setSpecialCharCount(Integer specialCharCount) {
        this.specialCharCount = specialCharCount;
    }

    public Boolean getNotUsername() {
        return notUsername;
    }

    public void setNotUsername(Boolean notUsername) {
        this.notUsername = notUsername;
    }
}
