package org.hzero.iam.domain.repository;

import java.util.List;

import org.hzero.iam.domain.entity.Domain;
import org.hzero.mybatis.base.BaseRepository;

/**
 * 门户分配资源库
 *
 * @author minghui.qiu@hand-china.com 2019-06-27 20:50:16
 * @author xianzhi.chen 缓存处理优化
 */
public interface DomainRepository extends BaseRepository<Domain> {

    /**
     *
     * 查询域名汇总信息
     *
     * @param domain 域名对象
     * @return 域名集合
     */
    public List<Domain> selectByOptions(Domain domain);

    /**
     *
     * 查询域名明细
     *
     * @param domainId 域名ID
     * @return 域名信息
     */
    public Domain selectByDomainId(Long domainId);

    /**
     *
     * 初始化域名缓存
     */
    public void initCacheDomain();

    /**
     *
     * 域名缓存
     *
     * @param domain 域名信息
     */
    public void insertDomainCache(Domain domain);

    /**
     *
     * 删除域名缓存
     * @param domainId 域名信息ID
     * @return 影响行数
     */
    public void deleteDomainCache(Long domainId);

}
