package com.mivideo.mifm.cpplugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 用一个32位的int值，来代表最多31种功能的支持
 * 例如：
 * CAN_CHANGE_DEFINITION 就是 1 (2的0次幂)
 * CAN_GET_REAL_PLAY_URL 就是 2 (2的1次幂)
 * 依次类推
 */
public enum PluginAbility {
    CAN_CHANGE_DEFINITION(0), // 能够支持调整分辨率
    CAN_QUERY_PLAY_URL(1), // 能够支持query播放地址
    RESERVED_BOOLEAN_STATE_2(2),
    RESERVED_BOOLEAN_STATE_3(3),
    RESERVED_BOOLEAN_STATE_4(4),
    RESERVED_BOOLEAN_STATE_5(5),
    RESERVED_BOOLEAN_STATE_6(6),
    RESERVED_BOOLEAN_STATE_7(7),
    RESERVED_BOOLEAN_STATE_8(8),
    RESERVED_BOOLEAN_STATE_9(9),
    RESERVED_BOOLEAN_STATE_10(10),
    RESERVED_BOOLEAN_STATE_11(11),
    RESERVED_BOOLEAN_STATE_12(12),
    RESERVED_BOOLEAN_STATE_13(13),
    RESERVED_BOOLEAN_STATE_14(14),
    RESERVED_BOOLEAN_STATE_15(15),
    RESERVED_BOOLEAN_STATE_16(16),
    RESERVED_BOOLEAN_STATE_17(17),
    RESERVED_BOOLEAN_STATE_18(18),
    RESERVED_BOOLEAN_STATE_19(19),
    RESERVED_BOOLEAN_STATE_20(20),
    RESERVED_BOOLEAN_STATE_21(21),
    RESERVED_BOOLEAN_STATE_22(22),
    RESERVED_BOOLEAN_STATE_23(23),
    RESERVED_BOOLEAN_STATE_24(24),
    RESERVED_BOOLEAN_STATE_25(25),
    RESERVED_BOOLEAN_STATE_26(26),
    RESERVED_BOOLEAN_STATE_27(27),
    RESERVED_BOOLEAN_STATE_28(28),
    RESERVED_BOOLEAN_STATE_29(29),
    RESERVED_BOOLEAN_STATE_30(30);

    public int offset;

    PluginAbility(int offset) {
        this.offset = offset;
    }

    /**
     * 构建：将支持的ability属性压缩为一个32位数
     */
    public static int format(PluginAbility... array) {
        int flag = 0;
        if (array == null || array.length == 0) {
            return flag;
        }
        List<PluginAbility> pas = new ArrayList<PluginAbility>();
        for (PluginAbility pa : array) {
            if (pa != null && !pas.contains(pa)) {
                // 去重去null
                pas.add(pa);
            }
        }
        for (PluginAbility pa : pas) {
            int offset = pa.offset - 1;
            if (offset < 0) {
                flag = flag + 1;
            } else {
                int v = 2 << offset;
                flag = flag + v;
            }
        }
        return flag;
    }

    /**
     * 解析：flag中是否包含某个ability
     */
    public static boolean match(int flag, PluginAbility pa) {
        if (flag <= 0 || pa == null) {
            return false;
        }
        if (((flag >> pa.offset) & 0x0001) == 1) {
            return true;
        } else {
            return false;
        }
    }
}
