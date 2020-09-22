package org.hzero.iam.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.user.UserType;
import org.hzero.iam.api.dto.PasswordDTO;
import org.hzero.iam.api.dto.UserEmployeeAssignDTO;
import org.hzero.iam.domain.entity.User;

/**
 * 用户应用服务
 *
 * @author bojiangzhou 2019/04/19 优化代码
 * @author allen 2018/6/29
 */
public interface UserService {

    //
    // 用户注册
    // ------------------------------------------------------------------------------

    /**
     * 企业用户注册
     *
     * @param user 用户信息
     * @return User
     */
    User register(User user);

    //
    // 用户管理
    // ------------------------------------------------------------------------------

    /**
     * 创建用户
     *
     * @param user 用户
     * @return 用户
     */
    User createUser(User user);

    /**
     * 创建用户(内部调用)
     *
     * @param user 用户
     * @return 用户
     */
    User createUserInternal(User user);

    /**
     * 更新用户
     *
     * @param user 用户
     * @return 用户
     */
    User updateUser(User user);

    /**
     * 更新用户(内部调用)
     *
     * @param user 用户
     */
    User updateUserInternal(User user);

    /**
     * 导入时创建用户
     *
     * @param user 用户
     * @return 用户
     */
    User importCreateUser(User user);

    /**
     * 锁定用户
     *
     * @param userId         用户ID
     * @param organizationId 租户ID
     */
    void lockUser(Long userId, Long organizationId);

    /**
     * 解锁用户
     *
     * @param userId         用户ID
     * @param organizationId 租户ID
     */
    void unlockUser(Long userId, Long organizationId);

    /**
     * 启用用户
     *
     * @param userId         用户ID
     * @param organizationId 租户ID
     */
    void frozenUser(Long userId, Long organizationId);

    /**
     * 禁用用户
     *
     * @param userId         用户ID
     * @param organizationId 租户ID
     */
    void unfrozenUser(Long userId, Long organizationId);

    /**
     * 管理员全局层更新用户密码
     *
     * @param userId         用户ID
     * @param organizationId 租户ID
     * @param password       密码
     */
    void updateUserPassword(Long userId, Long organizationId, String password);

    /**
     * 查询所有国际冠码
     *
     * @return 冠码
     */
    List<Map<String, String>> listIDD();

    /**
     * 通过手机号更新密码
     *
     * @param passwordDTO 密码DTO
     */
    void updateUserPasswordByPhone(PasswordDTO passwordDTO, UserType userType);

    /**
     * 通过邮箱号更新密码
     *
     * @param passwordDTO 密码DTO
     */
    void updateUserPasswordByEmail(PasswordDTO passwordDTO, UserType userType);

    /**
     * 根据手机、邮箱找回密码
     *
     * @param account       手机、邮箱
     * @param userType      用户类型
     * @param businessScope 验证码业务范围
     * @param password      新密码
     * @param captchaKey    验证码key
     * @param captcha       验证码
     */
    void updatePasswordByAccount(String account, UserType userType, String businessScope,
                                 String password, String captchaKey, String captcha);

    /**
     * 重置密码
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    void resetUserPassword(Long userId, Long tenantId);

    /**
     * 分页查询用户员工分配信息
     *
     * @param pageRequest    分页参数
     * @param organizationId 租户ID
     * @param userId         用户ID
     * @param params         查询参数
     * @return Page<UserEmployeeAssignDTO>
     */
    Page<UserEmployeeAssignDTO> pageUserEmployeeAssign(PageRequest pageRequest, Long organizationId, Long userId,
                    UserEmployeeAssignDTO params);
}
