package org.hzero.iam.domain.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.iam.domain.entity.RoleAuthDataLine;
import org.hzero.mybatis.base.BaseRepository;

import java.util.List;

/**
 * 角色单据权限管理行资源库
 *
 * @author qingsheng.chen@hand-china.com 2019-06-14 11:49:29
 */
public interface RoleAuthDataLineRepository extends BaseRepository<RoleAuthDataLine> {

    /**
     * 查询角色数据权限行
     *
     * @param authDataId  数据权限行
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 角色数据权限行
     */
    List<RoleAuthDataLine> pageRoleAuthDataLine(Long authDataId, Long tenantId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询采购组织
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 采购组织列表
     */
    Page<RoleAuthDataLine> pagePurOrg(Long tenantId, Long roleId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询采购员
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 采购员列表
     */
    Page<RoleAuthDataLine> pagePurAgent(Long tenantId, Long roleId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询值集
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 值集列表
     */
    Page<RoleAuthDataLine> pageLov(Long tenantId, Long roleId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询值集视图
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 值集视图列表
     */
    Page<RoleAuthDataLine> pageLovView(Long tenantId, Long roleId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询数据源
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 数据源列表
     */
    Page<RoleAuthDataLine> pageDatasource(Long tenantId, Long roleId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询数据组
     *
     * @param tenantId    租户ID
     * @param roleId      角色ID
     * @param groupCode    数据编码
     * @param groupName    数据名称
     * @param pageRequest 分页信息
     * @return 数据源列表
     */
    Page<RoleAuthDataLine> pageDataGroup(Long tenantId, Long roleId, String groupCode, String groupName, PageRequest pageRequest);

    /**
     * 分页查询采购组织
     *
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 采购组织列表
     */
    Page<RoleAuthDataLine> pagePurOrgAll(Long tenantId,String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询采购员
     *
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 采购员列表
     */
    Page<RoleAuthDataLine> pagePurAgentAll(Long tenantId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询值集
     *
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 值集列表
     */
    Page<RoleAuthDataLine> pageLovAll(Long tenantId, String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询值集视图
     *
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 值集视图列表
     */
    Page<RoleAuthDataLine> pageLovViewAll(Long tenantId,  String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询数据源
     *
     * @param tenantId    租户ID
     * @param dataCode    数据编码
     * @param dataName    数据名称
     * @param pageRequest 分页信息
     * @return 数据源列表
     */
    Page<RoleAuthDataLine> pageDatasourceAll(Long tenantId,  String dataCode, String dataName, PageRequest pageRequest);

    /**
     * 分页查询数据组
     *
     * @param tenantId    租户ID
     * @param groupCode    数据编码
     * @param groupName    数据名称
     * @param pageRequest 分页信息
     * @return 数据源列表
     */
    Page<RoleAuthDataLine> pageDataGroupAll(Long tenantId, String groupCode, String groupName, PageRequest pageRequest);

    /**
     * 查询排除目标角色已经存在的权限数据
     *
     * @param organizationId 租户id
     * @param roleId 源角色Id
     * @param copyRoleId 目标角色Id
     * @param compDocType 权限类型
     * @return List<RoleAuthDataLine>
     */
    List<RoleAuthDataLine> selectCompliantRoleAuthDatas(Long organizationId, Long roleId, Long copyRoleId, String compDocType);
}
