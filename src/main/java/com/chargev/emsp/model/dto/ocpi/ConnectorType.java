package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConnectorType {
    CHADEMO("CHADEMO"),
    CHAOJI("CHAOJI"),
    DOMESTIC_A("DOMESTIC_A"),
    DOMESTIC_B("DOMESTIC_B"),
    DOMESTIC_C("DOMESTIC_C"),
    DOMESTIC_D("DOMESTIC_D"),
    DOMESTIC_E("DOMESTIC_E"),
    DOMESTIC_F("DOMESTIC_F"),
    DOMESTIC_G("DOMESTIC_G"),
    DOMESTIC_H("DOMESTIC_H"),
    DOMESTIC_I("DOMESTIC_I"),
    DOMESTIC_J("DOMESTIC_J"),
    DOMESTIC_K("DOMESTIC_K"),
    DOMESTIC_L("DOMESTIC_L"),
    DOMESTIC_M("DOMESTIC_M"),
    DOMESTIC_N("DOMESTIC_N"),
    DOMESTIC_O("DOMESTIC_O"),
    GBT_AC("GBT_AC"),
    GBT_DC("GBT_DC"),
    IEC_60309_2_SINGLE_16("IEC_60309_2_SINGLE_16"),
    IEC_60309_2_THREE_16("IEC_60309_2_THREE_16"),
    IEC_60309_2_THREE_32("IEC_60309_2_THREE_32"),
    IEC_60309_2_THREE_64("IEC_60309_2_THREE_64"),
    IEC_62196_T1("IEC_62196_T1"),
    IEC_62196_T1_COMBO("IEC_62196_T1_COMBO"),
    IEC_62196_T2("IEC_62196_T2"),
    IEC_62196_T2_COMBO("IEC_62196_T2_COMBO"),
    IEC_62196_T3A("IEC_62196_T3A"),
    IEC_62196_T3C("IEC_62196_T3C"),
    NEMA_5_20("NEMA_5_20"),
    NEMA_6_30("NEMA_6_30"),
    NEMA_6_50("NEMA_6_50"),
    NEMA_10_30("NEMA_10_30"),
    NEMA_10_50("NEMA_10_50"),
    NEMA_14_30("NEMA_14_30"),
    NEMA_14_50("NEMA_14_50"),
    PANTOGRAPH_BOTTOM_UP("PANTOGRAPH_BOTTOM_UP"),
    PANTOGRAPH_TOP_DOWN("PANTOGRAPH_TOP_DOWN"),
    TESLA_R("TESLA_R"),
    TESLA_S("TESLA_S");

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ConnectorType fromValue(String text) {
        for (ConnectorType b : ConnectorType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
