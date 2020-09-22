package org.hzero.iam.domain;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hzero.iam.BaseTest;
import org.hzero.iam.domain.entity.Label;
import org.hzero.iam.domain.entity.LabelRel;
import org.hzero.iam.domain.repository.LabelRelRepository;
import org.hzero.iam.domain.repository.LabelRepository;
import org.hzero.iam.infra.constant.LabelAssignType;
import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.util.Sqls;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 标签关系表 资源库 测试类
 *
 * @author bo.he02@hand-china.com 2020-04-26 17:15:19
 */
public class LabelRelRepositoryTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(LabelRelRepositoryTest.class);

    /**
     * 标签类型: 测试
     */
    private static final String LABEL_TYPE = "TEST";
    /**
     * 标签层级: 租户
     */
    private static final String LABEL_FD_LEVEL = "organization";
    /**
     * 标签数据类型：测试
     */
    private static final String LABEL_DATA_TYPE = "TEST";
    /**
     * 标签tag: 测试
     */
    private static final String LABEL_TAG = "TEST_" + UUID.randomUUID();

    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelRelRepository labelRelRepository;

    @BeforeClass
    public static void beforeClass() {
        logger.info("\n\nTest Label Tag: {}\n\n", LABEL_TAG);
    }

    /**
     * 先添加标签，再删除标签
     */
    @Test
    public void test001() {
        this.process(() -> {
            String type = LABEL_DATA_TYPE;
            Long dataId = -10001L;

            List<Label> labels = this.selectLabels(Triad.of(1, 1, 1));


            // 添加标签
            this.labelRelRepository.addLabels(type, dataId, LabelAssignType.MANUAL, labels);
            // 查询关系
            List<LabelRel> dbLabelRels = this.labelRelRepository.selectLabelsByDataTypeAndDataId(type, dataId);
            Map<String, Label> dbLabelMap = dbLabelRels.stream().map(LabelRel::getLabel).collect(Collectors.toMap(Label::getName, t -> t));

            assert labels.stream().allMatch(label -> dbLabelMap.containsKey(label.getName()));


            // 移除标签
            this.labelRelRepository.removeLabels(type, dataId, labels);
            // 查询关系
            dbLabelRels = this.labelRelRepository.selectLabelsByDataTypeAndDataId(type, dataId);

            assert CollectionUtils.isEmpty(dbLabelRels);
        }, 3, Triad.of(1, 1, 1));
    }

    /**
     * 先添加不同的标签，再添加相同的标签
     */
    @Test
    public void test002() {
        this.process(() -> {
            String type = LABEL_DATA_TYPE;
            Long dataId = -10001L;
            LabelAssignType assignType = LabelAssignType.MANUAL;

            // 添加标签
            this.addLabels(type, dataId, assignType, Triad.of(1, 1, 1));

            // 添加标签
            this.addLabels(type, dataId, assignType, Triad.of(1, 0, 1));

            try {
                // 添加标签
                this.addLabels(type, dataId, assignType, Triad.of(1, 1, 1));
            } catch (Exception e) {
                logger.error("Add Labels Error: ", e);
                assert true;
                return;
            }

            assert false;
        }, 3, Triad.of(1, 1, 1), Triad.of(1, 0, 1));
    }

    /**
     * 添加标签
     *
     * @param type       数据类型
     * @param dataId     数据ID
     * @param assignType 分配类型
     * @param triad      标签数据三元组
     */
    private void addLabels(String type, Long dataId, LabelAssignType assignType, Triad triad) {
        List<Label> labels = this.selectLabels(triad);
        // 添加标签
        this.labelRelRepository.addLabels(type, dataId, assignType, labels);
        // 查询关系
        List<LabelRel> dbLabelRels = this.labelRelRepository.selectLabelsByDataTypeAndDataId(type, dataId);
        Map<String, Label> dbLabelMap = dbLabelRels.stream().map(LabelRel::getLabel).collect(Collectors.toMap(Label::getName, t -> t));

        assert labels.stream().allMatch(label -> dbLabelMap.containsKey(label.getName()));
    }

    /**
     * 查询标签数据
     *
     * @param triad 参数三元组
     * @return 满足条件的标签对象
     */
    private List<Label> selectLabels(Triad triad) {
        return this.labelRepository.selectByCondition(Condition.builder(Label.class)
                .andWhere(Sqls.custom()
                        .andEqualTo(Label.FIELD_TAG, LABEL_TAG)
                        .andEqualTo(Label.FIELD_INHERIT_FLAG, triad.inheritFlag)
                        .andEqualTo(Label.FIELD_PRESET_FLAG, triad.presetFlag)
                        .andEqualTo(Label.FIELD_VISIBLE_FLAG, triad.visibleFlag)
                ).build());
    }

    /**
     * 删除标签数据
     *
     * @param triad 三元组
     */
    private void deleteLabels(Triad triad) {
        // 查询标签
        List<Label> labels = this.selectLabels(triad);
        if (CollectionUtils.isNotEmpty(labels)) {
            // 查询标签关联数据
            List<LabelRel> labelRels = this.labelRelRepository.selectByCondition(Condition.builder(LabelRel.class)
                    .andWhere(Sqls.custom()
                            .andIn(LabelRel.FIELD_LABEL_ID, labels.stream().map(Label::getId).collect(Collectors.toSet()))
                    ).build());
            if (CollectionUtils.isNotEmpty(labelRels)) {
                // 删除标签关系
                labelRels.forEach(this.labelRelRepository::deleteByPrimaryKey);
            }

            // 删除标签
            labels.forEach(this.labelRepository::deleteByPrimaryKey);
        }
    }

    /**
     * 批量初始化标签
     *
     * @param triad 三元组
     */
    private void initLabelDataByType(Triad triad, Integer count) {
        for (int i = 1; i <= count; i++) {
            this.labelRepository.insertSelective(this.builtLabel(String.format("TEST_LABEL_%d%d%d_%03d",
                    triad.inheritFlag, triad.presetFlag, triad.visibleFlag, i),
                    triad.inheritFlag, triad.presetFlag, triad.visibleFlag));
        }
    }

    /**
     * 创建标签
     *
     * @param name        名称
     * @param inheritFlag 是否可继承
     * @param presetFlag  是否内置标签
     * @param visibleFlag 是否页面可见
     * @return 标签对象
     */
    private Label builtLabel(String name, Integer inheritFlag, Integer presetFlag, Integer visibleFlag) {
        Label label = new Label();
        label.setName(name);

        label.setInheritFlag(inheritFlag);
        label.setPresetFlag(presetFlag);
        label.setVisibleFlag(visibleFlag);

        label.setType(LABEL_TYPE);
        label.setFdLevel(LABEL_FD_LEVEL);
        label.setTag(LABEL_TAG);

        return label;
    }

    /**
     * 执行处理逻辑
     *
     * @param run    执行的逻辑
     * @param count  初始化的数据的个数
     * @param triads 初始化数据的原始参数
     */
    private void process(Runnable run, int count, Triad... triads) {
        try {
            // 初始化数据
            if (ArrayUtils.isEmpty(triads)) {
                return;
            }
            for (Triad triad : triads) {
                this.initLabelDataByType(triad, count);
            }

            // 执行逻辑
            run.run();
        } finally {
            // 移除初始化的数据
            if (ArrayUtils.isNotEmpty(triads)) {
                for (Triad triad : triads) {
                    this.deleteLabels(triad);
                }
            }
        }
    }

    /**
     * 初始化数据参数三元组
     */
    private static class Triad {
        private Integer inheritFlag;
        private Integer presetFlag;
        private Integer visibleFlag;

        private Triad(Integer inheritFlag, Integer presetFlag, Integer visibleFlag) {
            this.inheritFlag = inheritFlag;
            this.presetFlag = presetFlag;
            this.visibleFlag = visibleFlag;
        }

        public static Triad of(Integer inheritFlag, Integer presetFlag, Integer visibleFlag) {
            return new Triad(inheritFlag, presetFlag, visibleFlag);
        }
    }
}
