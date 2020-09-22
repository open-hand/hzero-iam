package org.hzero.iam.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.iam.app.service.DomainService;
import org.hzero.iam.domain.entity.Domain;
import org.hzero.iam.domain.repository.DomainRepository;

/**
 * 门户分配应用服务默认实现
 *
 * @author minghui.qiu@hand-china.com 2019-06-27 20:50:16
 */
@Service
public class DomainServiceImpl implements DomainService {

    @Autowired
    private DomainRepository domainRepository;

    @Override
    public Page<Domain> selectByOptions(PageRequest pageRequest, Domain domain) {
        return PageHelper.doPageAndSort(pageRequest, () -> domainRepository.selectByOptions(domain));
    }

    @Override
    public Domain selectByDomainId(Long domainId) {
        return domainRepository.selectByDomainId(domainId);
    }

    @Override
    public int updateDomain(Domain domain) {
        domain.vaidateDomainUrl(domainRepository);
        int cnt = domainRepository.updateOptional(domain, Domain.FIELD_DOMAIN_URL, Domain.FIELD_SSO_TYPE_CODE,
                        Domain.FIELD_SSO_SERVER_URL, Domain.FIELD_SSO_LOGIN_URL, Domain.FIELD_SSO_LOGOUT_URL,
                        Domain.FIELD_SSO_CLIENT_ID, Domain.FIELD_SSO_CLIENT_PWD, Domain.FIELD_SSO_USER_INFO,
                        Domain.FIELD_SAML_META_URL, Domain.FIELD_CLIENT_HOST_URL, Domain.FIELD_COMPANY_ID,
                        Domain.FIELD_LOGIN_NAME_FIELD, Domain.FIELD_REMARK);
        domainRepository.insertDomainCache(domain);
        return cnt;
    }

    @Override
    public int insertDomain(Domain domain) {
        domain.vaidateDomainUrl(domainRepository);
        int cnt = domainRepository.insertSelective(domain);
        domainRepository.insertDomainCache(domain);
        return cnt;
    }

    @Override
    public int deleteDomain(Domain domain) {
        int cnt = domainRepository.deleteByPrimaryKey(domain);
        domainRepository.deleteDomainCache(domain.getDomainId());
        return cnt;
    }

}
