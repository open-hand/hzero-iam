//package org.hzero.iam.app.service.impl
//
//import org.apache.commons.lang.math.RandomUtils
//import org.hzero.iam.IamApplication
//
//import org.hzero.iam.domain.entity.Tenant
//import org.hzero.iam.domain.entity.TenantConfig
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootContextLoader
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ContextConfiguration
//import spock.lang.Specification
//import spock.lang.Title
//
//@SpringBootTest(classes = IamApplication.class)
//@ContextConfiguration(loader = SpringBootContextLoader.class)
//@Title("Tenant create test")
//public class TenantCreateTest extends Specification {
//    @Autowired
//    TenantService tenantService;
//
//
//    def "tenant create"() {
//        given: "初始化参数"
//        Tenant tenant = new Tenant();
//        tenant.setTenantName(tenantName)
//
//        TenantConfig config1 = new TenantConfig("website", "http://www.official.com")
//        TenantConfig config2 = new TenantConfig("owner", "1")
//        List<TenantConfig> configList = new ArrayList<>();
//        configList.add(config1)
//        configList.add(config2)
//
//        tenant.setTenantConfigs(configList)
//
//        when: "获取租户管理员角色继承模板"
//        tenant = tenantService.createTenant(tenant)
//
//        then: "获取成功"
//        notThrown()
//
//        where:
//        tenantName|n
//        "TEST_租户_" + RandomUtils.nextInt(100)|1
//
//    }
//}
