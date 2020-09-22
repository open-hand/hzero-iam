package org.hzero.iam.infra.init;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hzero.core.message.MessageAccessor;
import org.hzero.iam.app.service.FieldPermissionService;
import org.hzero.iam.app.service.OpenAppService;
import org.hzero.iam.config.IamProperties;
import org.hzero.iam.domain.repository.ClientRepository;
import org.hzero.iam.domain.repository.LdapRepository;
import org.hzero.iam.domain.repository.PasswordPolicyRepository;
import org.hzero.iam.domain.repository.UserRepository;
import org.hzero.iam.domain.service.AuthorityDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.choerodon.mybatis.pagehelper.Dialect;

/**
 * <p>
 * 应用启动时缓存初始化数据
 * </p>
 *
 * @author jiaxu.cui@hand-china.com
 */
@Component
public class IamDataInit implements SmartInitializingSingleton {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamDataInit.class);

    private final OpenAppService openAppService;
    private final PasswordPolicyRepository passwordPolicyRepository;
    private final UserRepository userRepository;
    private final LdapRepository ldapRepository;
    private final ClientRepository clientRepository;
    private final FieldPermissionService fieldPermissionService;
    private final AuthorityDomainService authorityDomainService;
    private final IamProperties properties;

    @Autowired
    public IamDataInit(OpenAppService openAppService,
                       PasswordPolicyRepository passwordPolicyRepository,
                       UserRepository userRepository,
                       LdapRepository ldapRepository,
                       ClientRepository clientRepository,
                       FieldPermissionService fieldPermissionService,
                       AuthorityDomainService authorityDomainService,
                       IamProperties properties,
                       Dialect dialect) {
        this.openAppService = openAppService;
        this.passwordPolicyRepository = passwordPolicyRepository;
        this.userRepository = userRepository;
        this.ldapRepository = ldapRepository;
        this.clientRepository = clientRepository;
        this.fieldPermissionService = fieldPermissionService;
        this.authorityDomainService = authorityDomainService;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 加入消息文件
        MessageAccessor.addBasenames("classpath:messages/messages_hiam");

        LOGGER.info("Start init redis cache.");

        ExecutorService executorService = Executors.newFixedThreadPool(12, new ThreadFactoryBuilder().setNameFormat("HiamRedisInit-%d").build());
        if (properties.getInitCache().isOpenLoginWay()) {
            executorService.submit(openAppService::saveOpenAppCache);
        }

        if (properties.getInitCache().isPasswordPolicy()) {
            executorService.submit(passwordPolicyRepository::initCachePasswordPolicy);
        }

        if (properties.getInitCache().isLdap()) {
            executorService.submit(ldapRepository::initCacheLdap);
        }

        if (properties.getInitCache().isClient()) {
            executorService.submit(clientRepository::initCacheClient);
        }

        if (properties.getInitCache().isUser()) {
            executorService.submit(userRepository::initUsers);
        }

        if (properties.getInitCache().isFieldPermission()) {
            executorService.submit(fieldPermissionService::restorePermission);
        }

        if (properties.getInitCache().isDocAuth()) {
            executorService.submit(authorityDomainService::initDocAuthCache);
        }

        executorService.shutdown();
        LOGGER.info("Finish init redis cache.");
    }

}
