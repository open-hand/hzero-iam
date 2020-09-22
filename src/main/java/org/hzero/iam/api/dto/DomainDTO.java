package org.hzero.iam.api.dto;

import java.io.Serializable;
import java.util.StringJoiner;

public class DomainDTO implements Serializable{
    private static final long serialVersionUID = -6625864894934365970L;
    
    private Long tenantId;
    private Long companyId;
    private Long domainId;
    private String domainUrl;
    private String ssoTypeCode;
    private String ssoServerUrl;
    private String ssoLoginUrl;
    private String ssoLogoutUrl;
    private String clientHostUrl;
    private String ssoClientId;
    private String ssoClientPwd;
    private String ssoUserInfo;
    private String samlMetaUrl;
    private String loginNameField;
    private String tenantNum;

    /**
     * @return
     */
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }


    /**
     * @return 域名
     */
    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    /**
     * @return CAS|CAS2|SAML|IDM|NULL
     */
    public String getSsoTypeCode() {
        return ssoTypeCode;
    }

    public void setSsoTypeCode(String ssoTypeCode) {
        this.ssoTypeCode = ssoTypeCode;
    }

    /**
     * @return 单点认证服务器地址
     */
    public String getSsoServerUrl() {
        return ssoServerUrl;
    }

    public void setSsoServerUrl(String ssoServerUrl) {
        this.ssoServerUrl = ssoServerUrl;
    }

    /**
     * @return 单点登录地址
     */
    public String getSsoLoginUrl() {
        return ssoLoginUrl;
    }

    public void setSsoLoginUrl(String ssoLoginUrl) {
        this.ssoLoginUrl = ssoLoginUrl;
    }

    /**
     * @return 客户端URL
     */
    public String getClientHostUrl() {
        return clientHostUrl;
    }

    public void setClientHostUrl(String clientHostUrl) {
        this.clientHostUrl = clientHostUrl;
    }

    public String getSsoClientId() {
        return ssoClientId;
    }

    public void setSsoClientId(String ssoClientId) {
        this.ssoClientId = ssoClientId;
    }

    public String getSsoClientPwd() {
        return ssoClientPwd;
    }

    public void setSsoClientPwd(String ssoClientPwd) {
        this.ssoClientPwd = ssoClientPwd;
    }

    public String getSsoUserInfo() {
        return ssoUserInfo;
    }

    public void setSsoUserInfo(String ssoUserInfo) {
        this.ssoUserInfo = ssoUserInfo;
    }

    public String getLoginNameField() {
        return loginNameField;
    }

    public void setLoginNameField(String loginNameField) {
        this.loginNameField = loginNameField;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DomainDTO.class.getSimpleName() + "[", "]")
                .add("domainId=" + domainId)
                .add("domainUrl='" + domainUrl + "'")
                .add("ssoTypeCode='" + ssoTypeCode + "'")
                .add("ssoServerUrl='" + ssoServerUrl + "'")
                .add("ssoLoginUrl='" + ssoLoginUrl + "'")
                .add("clientHostUrl='" + clientHostUrl + "'")
                .add("ssoClientId='" + ssoClientId + "'")
                .add("ssoClientPwd='" + ssoClientPwd + "'")
                .add("ssoUserInfo='" + ssoUserInfo + "'")
                .add("loginNameField='" + loginNameField + "'")
                .add("tenantNum='" + tenantNum + "'")
                .toString();
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getSsoLogoutUrl() {
      return ssoLogoutUrl;
    }

    public void setSsoLogoutUrl(String ssoLogoutUrl) {
      this.ssoLogoutUrl = ssoLogoutUrl;
    }

    public String getSamlMetaUrl() {
      return samlMetaUrl;
    }

    public void setSamlMetaUrl(String samlMetaUrl) {
      this.samlMetaUrl = samlMetaUrl;
    }

    public String getTenantNum() {
        return tenantNum;
    }

    public void setTenantNum(String tenantNum) {
        this.tenantNum = tenantNum;
    }
}
