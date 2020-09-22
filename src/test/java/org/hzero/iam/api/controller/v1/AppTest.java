package org.hzero.iam.api.controller.v1;

import java.util.Arrays;

import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.domain.entity.RolePermission;
import org.hzero.iam.domain.entity.RolePermissionSet;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * 描述
 * </p>
 *
 * @author mingwei.liu@hand-china.com 2018/8/13
 */
public class AppTest {
    private String code;

    enum EnumTest {
        LOCAL("local"), REMOTE("local");
        private String value;

        EnumTest(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void useCode() {
        System.out.println("parent-> " + this.code);
    }

    public static final void main(String[] args) {
        AppTestChild appTestChild = new AppTestChild();

        appTestChild.setCode("abc");
        appTestChild.useCode();

        System.out.println(EnumTest.valueOf("LOCAL"));
    }

    public static void testJson() throws Exception {
        Role role = new Role();
        role.setId(38L);
        role.setCode("role/organization/custom/[2.organization.0]/xxx");
        role.setLevel("organization");
        role.setName("XXX管理员");
        role.setParentRoleId(2L);
        role.setTenantId(1L);
        role.setObjectVersionNumber(4L);
        RolePermissionSet rolePermissionSet = new RolePermissionSet();

        /**
         * #1
         */
        rolePermissionSet.setPermissionSetId(3L);

        /**
         * #2
         */
        RolePermissionSet rolePermissionSet2 = new RolePermissionSet();
        RolePermission rolePermission = new RolePermission(null, 4L, "Y", "N", "PS", role.getTenantId());
        RolePermission rolePermission2 = new RolePermission(null, 9L, "Y", "N", "PS", role.getTenantId());
        rolePermissionSet2.setRolePermissions(Arrays.asList(rolePermission, rolePermission2));

        //role.setRolePermissionSets(Arrays.asList(rolePermissionSet, rolePermissionSet2));

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(role));
    }
}

class AppTestChild extends AppTest {

    @Override
    public void useCode() {
        super.useCode();
    }
}
