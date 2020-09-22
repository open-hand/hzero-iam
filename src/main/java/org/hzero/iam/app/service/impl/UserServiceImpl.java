package org.hzero.iam.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.boot.oauth.domain.service.BaseUserService;
import org.hzero.boot.oauth.domain.service.UserPasswordService;
import org.hzero.boot.platform.encrypt.EncryptClient;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.user.UserType;
import org.hzero.iam.api.dto.PasswordDTO;
import org.hzero.iam.api.dto.UserEmployeeAssignDTO;
import org.hzero.iam.app.service.UserService;
import org.hzero.iam.domain.entity.User;
import org.hzero.iam.domain.repository.UserRepository;
import org.hzero.iam.domain.service.user.*;
import org.hzero.iam.domain.service.user.interceptor.UserInterceptorChainManager;
import org.hzero.iam.domain.service.user.interceptor.UserOperation;
import org.hzero.iam.infra.common.utils.UserUtils;
import org.hzero.iam.infra.constant.Constants;
import org.hzero.iam.infra.feign.OauthAdminFeignClient;

/**
 * @author bojiangzhou 2019/04/19 代码优化
 * @author allen 2018/6/29
 */
@Service
public class UserServiceImpl implements UserService, AopProxy<UserServiceImpl> {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCreateService userCreateService;
    @Autowired
    private UserUpdateService userUpdateService;
    @Autowired
    private UserRegisterService userRegisterService;
    @Autowired
    private LovAdapter lovAdapter;
    @Autowired
    private UserCreateInternalService userCreateInternalService;
    @Autowired
    private OauthAdminFeignClient oauthAdminService;
    @Autowired
    private UserPasswordService userPasswordService;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    protected UserCheckService userCheckService;
    @Autowired
    private UserCaptchaService userCaptchaService;
    @Autowired
    private EncryptClient encryptClient;

    @Autowired
    private UserInterceptorChainManager userInterceptorChainManager;

    //
    // 用户注册
    // ------------------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.REGISTER_USER, user, (u) -> {
            // 注册用户
            userRegisterService.registerUser(u);
        });

        return user;
    }

    //
    // 用户管理
    // ------------------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.CREATE_USER, user, (u) -> {
            // 创建用户
            userCreateService.createUser(u);
        });

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUserInternal(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.CREATE_USER_INTERNAL, user, (u) -> {
            // 创建用户
            userCreateInternalService.createUser(u);
        });

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.UPDATE_USER, user, (u) -> {
            // 更新用户
            userUpdateService.updateUser(u);
        });

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUserInternal(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.UPDATE_USER_INTERNAL, user, (u) -> {
            // 更新用户
            userUpdateService.updateUser(u);
        });

        return user;
    }

    @Override
    public User importCreateUser(User user) {
        userInterceptorChainManager.doInterceptor(UserOperation.IMPORT_USER, user, (u) -> {
            // 内部创建用户
            userCreateInternalService.createUser(u);
        });

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockUser(Long userId, Long organizationId) {
        User user = userRepository.selectSimpleUserByIdAndTenantId(userId, organizationId);
        Assert.notNull(user, "hiam.warn.user.notFound");
        baseUserService.lockUser(userId, organizationId);
        //clean token
        oauthAdminService.invalidByUsername(user.getLoginName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(Long userId, Long organizationId) {
        baseUserService.unLockUser(userId, organizationId);
    }

    @Override
    public void frozenUser(Long userId, Long organizationId) {
        User user = userRepository.selectSimpleUserByIdAndTenantId(userId, organizationId);
        Assert.notNull(user, "hiam.warn.user.notFound");
        user.frozen();
        userRepository.updateOptional(user, User.FIELD_ENABLED);
        //clean token
        oauthAdminService.invalidByUsername(user.getLoginName());
    }

    @Override
    public void unfrozenUser(Long userId, Long organizationId) {
        User user = userRepository.selectSimpleUserByIdAndTenantId(userId, organizationId);
        Assert.notNull(user, "hiam.warn.user.notFound");
        user.unfrozen();
        userRepository.updateOptional(user, User.FIELD_ENABLED);
    }

    @Override
    public void updateUserPassword(Long userId, Long organizationId, String password) {
        User user = userRepository.selectSimpleUserByIdAndTenantId(userId, organizationId);
        if (user == null) {
            throw new CommonException("hiam.warn.user.notFound");
        }
        // 不让修改管理员密码
        if (user.getAdmin() != null && user.getAdmin()) {
            throw new CommonException("hiam.warn.user.modifyAdminPassDeny");
        }
        // 判断是否为当前登录用户
        if (userId.equals(UserUtils.getUserDetails().getUserId())) {
            throw new CommonException("hiam.warn.user.modifySelfPassDeny");
        }
        user.setOrganizationId(organizationId);
        updateUserPassword(user, password);
    }

    private void updateUserPassword(User user, String password) {
        password = encryptClient.decrypt(password);
        // 判断是否启用密码策略校验
        if (user.getCheckPasswordPolicy() == null || user.getCheckPasswordPolicy()) {
            // 检查密码是否符合密码策略
            userCheckService.checkPasswordPolicy(password, user.getOrganizationId());
        }
        userPasswordService.updateUserPassword(user.getId(), password);
    }

    @Override
    public List<Map<String, String>> listIDD() {
        List<Map<String, String>> result = new ArrayList<>(256);
        List<LovValueDTO> lov = lovAdapter.queryLovValue(Constants.IDD_LOV_CODE, Constants.SITE_TENANT_ID);
        lov = lov.stream().sorted(Comparator.comparing(LovValueDTO::getOrderSeq)).collect(Collectors.toList());
        lov.forEach(item -> {
            Map<String, String> map = new HashMap<>(BaseConstants.Digital.FOUR);
            map.put("internationalTelCode", item.getValue());
            map.put("internationalTelMeaning", item.getMeaning());
            result.add(map);
        });
        return result;
    }

    @Override
    public void updateUserPasswordByPhone(PasswordDTO passwordDTO, UserType userType) {
        User params = new User();
        params.setPhone(passwordDTO.getPhone());
        params.setUserType(userType.value());
        User user = userRepository.selectOne(params);
        if (user == null) {
            throw new CommonException("hiam.warn.user.notFoundWithPhone");
        }
        updateUserPassword(user, passwordDTO.getPassword());
    }

    @Override
    public void updateUserPasswordByEmail(PasswordDTO passwordDTO, UserType userType) {
        User params = new User();
        params.setEmail(passwordDTO.getEmail());
        params.setUserType(userType.value());
        User user = userRepository.selectOne(params);
        if (user == null) {
            throw new CommonException("hiam.warn.user.notFoundWithEmail");
        }
        updateUserPassword(user, passwordDTO.getPassword());
    }

    @Override
    public void updatePasswordByAccount(String account, UserType userType, String businessScope,
                                        String password, String captchaKey, String captcha) {
        userCaptchaService.validateCaptcha(captchaKey, captcha, account, userType, businessScope);
        User user = new User();
        if (account.contains(BaseConstants.Symbol.AT)) {
            user.setEmail(account);
        } else {
            user.setPhone(account);
        }
        user = userRepository.selectOne(user);
        Assert.notNull(user, MessageAccessor.getMessage("user.account.not-exists", new Object[]{account}).desc());

        userPasswordService.updateUserPassword(user.getId(), encryptClient.decrypt(password));
    }

    @Override
    public void resetUserPassword(Long userId, Long tenantId) {
        userPasswordService.resetUserPassword(userId, tenantId, false);
    }

    @Override
    public Page<UserEmployeeAssignDTO> pageUserEmployeeAssign(PageRequest pageRequest, Long organizationId, Long userId,
                    UserEmployeeAssignDTO params) {
        params.setOrganizationId(organizationId);
        params.setUserId(userId);
        return userRepository.pageUserEmployeeAssign(pageRequest, params);
    }

}
