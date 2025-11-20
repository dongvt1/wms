package org.jeecg.common.constant.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Rank enumeration class
 * 
 * Notice：This enumeration only applies to Tianjin Lingang HoldingsOAproject,The name and level of the rank are all written down（Need to be consistent with database configuration）
 * @date 2025-08-26
 * @author scott
 */
public enum PositionLevelEnum {

    // leadership level（grade1-3）
    CHAIRMAN("Chairman", 1, PositionType.LEADER),
    GENERAL_MANAGER("General manager", 2, PositionType.LEADER),
    VICE_GENERAL_MANAGER("副General manager", 3, PositionType.LEADER),

    // staff level（grade4-6）
    MINISTER("minister", 4, PositionType.STAFF),
    VICE_MINISTER("副minister", 5, PositionType.STAFF),
    STAFF("staff", 6, PositionType.STAFF);

    private final String name;
    private final int level;
    private final PositionType type;

    PositionLevelEnum(String name, int level, PositionType type) {
        this.name = name;
        this.level = level;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public PositionType getType() {
        return type;
    }

    /**
     * Rank type enumeration
     */
    public enum PositionType {
        STAFF("staff level"),
        LEADER("leadership level");

        private final String desc;

        PositionType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * Get enumeration based on rank name
     * @param name Rank name
     * @return Rank enumeration
     */
    public static PositionLevelEnum getByName(String name) {
        for (PositionLevelEnum position : values()) {
            if (position.getName().equals(name)) {
                return position;
            }
        }
        return null;
    }

    /**
     * 根据职级grade获取枚举
     * @param level 职级grade
     * @return Rank enumeration
     */
    public static PositionLevelEnum getByLevel(int level) {
        for (PositionLevelEnum position : values()) {
            if (position.getLevel() == level) {
                return position;
            }
        }
        return null;
    }

    /**
     * 根据Rank name判断是否为staff level
     * @param name Rank name
     * @return true-staff level，false-非staff level
     */
    public static boolean isStaffLevel(String name) {
        PositionLevelEnum position = getByName(name);
        return position != null && position.getType() == PositionType.STAFF;
    }

    /**
     * 根据Rank name判断是否为leadership level
     * @param name Rank name
     * @return true-leadership level，false-非leadership level
     */
    public static boolean isLeaderLevel(String name) {
        PositionLevelEnum position = getByName(name);
        return position != null && position.getType() == PositionType.LEADER;
    }

    /**
     * 比较两个职级的grade高低
     * @param name1 Rank name1
     * @param name2 Rank name2
     * @return positive number representationname1grade更高，negative number representationname2grade更高，0表示grade相同
     */
    public static int compareLevel(String name1, String name2) {
        PositionLevelEnum pos1 = getByName(name1);
        PositionLevelEnum pos2 = getByName(name2);

        if (pos1 == null || pos2 == null) {
            return 0;
        }

        // grade数字越小代表职级越高
        return pos2.getLevel() - pos1.getLevel();
    }

    /**
     * 判断是否为更高grade
     * @param currentName 当前Rank name
     * @param targetName 目标Rank name
     * @return true-Target level is higher，false-The target rank is not higher than the current rank
     */
    public static boolean isHigherLevel(String currentName, String targetName) {
        return compareLevel(targetName, currentName) > 0;
    }

    /**
     * 获取所有staff level名称
     * @return staff level名称列表
     */
    public static List<String> getStaffLevelNames() {
        return Arrays.asList(MINISTER.getName(), VICE_MINISTER.getName(), STAFF.getName());
    }

    /**
     * 获取所有leadership level名称
     * @return leadership level名称列表
     */
    public static List<String> getLeaderLevelNames() {
        return Arrays.asList(CHAIRMAN.getName(), GENERAL_MANAGER.getName(), VICE_GENERAL_MANAGER.getName());
    }

    /**
     * 获取所有Rank name（按grade排序）
     * @return 所有Rank name列表
     */
    public static List<String> getAllPositionNames() {
        return Arrays.asList(
                CHAIRMAN.getName(), GENERAL_MANAGER.getName(), VICE_GENERAL_MANAGER.getName(),
                MINISTER.getName(), VICE_MINISTER.getName(), STAFF.getName()
        );
    }

    /**
     * 获取指定grade范围的职级
     * @param minLevel 最小grade
     * @param maxLevel 最大grade
     * @return Rank name列表
     */
    public static List<String> getPositionsByLevelRange(int minLevel, int maxLevel) {
        return Arrays.stream(values())
                .filter(p -> p.getLevel() >= minLevel && p.getLevel() <= maxLevel)
                .map(PositionLevelEnum::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}