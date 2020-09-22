package org.hzero.iam.domain.service.role.observer;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import org.hzero.core.observer.Observer;
import org.hzero.iam.domain.entity.Label;
import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.domain.service.role.AbstractRoleLabelService;
import org.hzero.iam.infra.constant.LabelAssignType;

/**
 * 角色标签
 *
 * @author bojiangzhou 2020/07/08
 */
@Component
public class RoleLabelObserver extends AbstractRoleLabelService implements Observer<Role> {

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void update(@Nonnull Role role, Object... args) {
        // 父级角色
        Role parentRole = role.getParentRole();
        // 继承角色
        Role inheritRole = role.getInheritRole();

        // 需要添加的标签IDs
        Set<Long> needAddedLabelIds = Collections.emptySet();
        if (Objects.nonNull(parentRole)) {
            needAddedLabelIds = this.labelRelRepository
                    .selectInheritLabelIdsByDataTypeAndDataIds(Role.LABEL_DATA_TYPE, Collections.singleton(parentRole.getId()));
        }
        else if (Objects.nonNull(inheritRole)) {
            needAddedLabelIds = this.labelRelRepository
                    .selectLabelIdsByDataTypeAndDataIds(Role.LABEL_DATA_TYPE, Collections.singleton(inheritRole.getId()));
        }

        if (CollectionUtils.isNotEmpty(needAddedLabelIds)) {
            // 分配标签
            this.labelRelRepository.addLabels(Role.LABEL_DATA_TYPE, role.getId(), LabelAssignType.AUTO, needAddedLabelIds);

            // 获取角色标签视图
            List<Label> roleLabels = Optional.ofNullable(role.getRoleLabels()).orElse(new ArrayList<>());
            // 将可继承的标签加到视图标签中
            roleLabels.addAll(needAddedLabelIds.stream().map(labelId -> {
                Label label = new Label();
                label.setId(labelId);
                return label;
            }).collect(Collectors.toSet()));
            // 设置标签
            role.setRoleLabels(roleLabels);
        }
        super.handleRoleLabels(role);
    }
}
