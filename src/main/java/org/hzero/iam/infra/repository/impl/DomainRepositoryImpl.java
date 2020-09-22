package org.hzero.iam.infra.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.hzero.core.redis.RedisHelper;
import org.hzero.iam.api.dto.DomainDTO;
import org.hzero.iam.domain.entity.Domain;
import org.hzero.iam.domain.repository.DomainRepository;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.iam.infra.mapper.DomainMapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;

/**
 * 门户分配 资源库实现
 *
 * @author minghui.qiu@hand-china.com 2019-06-27 20:50:16
 */
@Component
public class DomainRepositoryImpl extends BaseRepositoryImpl<Domain> implements DomainRepository {

    @Autowired
    private DomainMapper domainMapper;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public List<Domain> selectByOptions(Domain domain) {
        return domainMapper.selectByOptions(domain);
    }

    @Override
    public Domain selectByDomainId(Long domainId) {
        return domainMapper.selectByDomainId(domainId);
    }

    @Override
    public void initCacheDomain() {
        List<Domain> domainList = domainMapper.selectByOptions(new Domain());
        domainList.forEach(this::insertDomainCache);
    }

    @Override
    public void insertDomainCache(Domain domain) {
        DomainDTO redisDomain = new DomainDTO();
        redisDomain.setDomainId(domain.getDomainId());
        redisDomain.setTenantId(domain.getTenantId());
        redisDomain.setCompanyId(domain.getCompanyId());
        redisDomain.setDomainUrl(domain.getDomainUrl());
        redisDomain.setSsoTypeCode(domain.getSsoTypeCode());
        redisDomain.setSsoServerUrl(domain.getSsoServerUrl());
        redisDomain.setSsoLoginUrl(domain.getSsoLoginUrl());
        redisDomain.setSsoLogoutUrl(domain.getSsoLogoutUrl());
        redisDomain.setSsoClientId(domain.getSsoClientId());
        redisDomain.setSsoClientPwd(domain.getSsoClientPwd());
        redisDomain.setSsoUserInfo(domain.getSsoUserInfo());
        redisDomain.setSamlMetaUrl(domain.getSamlMetaUrl());
        redisDomain.setClientHostUrl(domain.getClientHostUrl());
        redisDomain.setLoginNameField(domain.getLoginNameField());
        redisDomain.setTenantNum(domain.getTenantNum());

        redisHelper.hshPut(Constants.HIAM_DOMAIN, redisDomain.getDomainId().toString(), redisHelper.toJson(redisDomain));
    }

    @Override
    public void deleteDomainCache(Long domainId) {
        redisHelper.hshDelete(Constants.HIAM_DOMAIN, String.valueOf(domainId));
    }


}
