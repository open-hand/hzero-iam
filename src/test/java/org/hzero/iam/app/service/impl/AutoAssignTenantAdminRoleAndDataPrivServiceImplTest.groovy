package org.hzero.iam.app.service.impl

import org.hzero.iam.IamApplication
import org.hzero.iam.api.dto.TenantAdminRoleAndDataPrivAutoAssignmentDTO
import org.hzero.iam.domain.entity.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Title

/**
 * <p>
 * 描述
 * </p>
 *
 * @author mingwei.liu@hand-china.com 2018/7/31
 */
@SpringBootTest(classes = IamApplication.class)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@Title("Auto assign tenant admin role test")
@Timeout(1)
public class AutoAssignTenantAdminRoleAndDataPrivServiceImplTest extends Specification {
    @Autowired
    AutoAssignTenantAdminRoleAndDataPrivServiceImpl hiamAutoAssignTenantAdminRoleAndDataPrivService;


    def "test get admin role inherit template list"() {
        given: "初始化参数"
        TenantAdminRoleAndDataPrivAutoAssignmentDTO tenantAdminRoleAndDataPrivAutoAssignmentDTO = new TenantAdminRoleAndDataPrivAutoAssignmentDTO();
        List<Role> adminRoleInheritTemplateList = null;

        when: "获取租户管理员角色继承模板"
        adminRoleInheritTemplateList = hiamAutoAssignTenantAdminRoleAndDataPrivService.getAdminRoleInheritTemplateList(tenantAdminRoleAndDataPrivAutoAssignmentDTO);

        then: "获取成功"
        adminRoleInheritTemplateList != null;
        and: adminRoleInheritTemplateList.size() > 0;
    }
}
