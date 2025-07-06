package com.kryos.cloudImagebackend.manager.websocket.model;

import lombok.Getter;

/**
 * 图片编辑动作枚举
 */
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("放大操作", "ZOOM_IN"),
    ZOOM_OUT("缩小操作", "ZOOM_OUT"),
    ROTATE_LEFT("左旋操作", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋操作", "ROTATE_RIGHT"),
    ROTATE_CUSTOM("自定义角度旋转", "ROTATE_CUSTOM"),
    SCALE_RATIO("等比例放大", "SCALE_RATIO");

    private final String text;
    private final String value;

    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PictureEditActionEnum actionEnum : PictureEditActionEnum.values()) {
            if (actionEnum.value.equals(value)) {
                return actionEnum;
            }
        }
        return null;
    }
}