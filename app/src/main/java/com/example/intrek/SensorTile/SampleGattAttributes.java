/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.intrek.SensorTile;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String SERVICE1 = "-0000-1000-8000-00805f9b34fb";
    public static String SERVICE2 = "-0000-1000-8000-00805f9b34fb";
    public static String SERVICE3 = "-0001-11e1-9ab4-0002a5d5c51b";
    public static String chara4 = "-0001-11e1-ac36-0002a5d5c51b";

    private static final String COMMON_CHAR_UUID = "-11e1-ac36-0002a5d5c51b";
    public static final String COMMON_UUID_SERVICES = "-11e1-9ab4-0002a5d5c51b";
    public static final String COMMON_DEBUG_UUID_CHAR = "-000e" + COMMON_CHAR_UUID;
    public static final String COMMON_CONFIG_UUID_CHAR = "-000f" + COMMON_CHAR_UUID;
    public static final String BASE_FEATURE_COMMON_UUID = "-0001" + COMMON_CHAR_UUID;

    static {
        // Sample Services.
        attributes.put("00000000-000e" + COMMON_UUID_SERVICES, " DEBUG_SERVICE_UUID");
        attributes.put("00000000-000f" + COMMON_UUID_SERVICES, "CONFIG_CONTROL_SERVICE_UUID");
        attributes.put("00000000-0001" + COMMON_UUID_SERVICES, "FEATURES_DATA");

        // Sample Characteristics.

        attributes.put("00000001" + COMMON_DEBUG_UUID_CHAR, "DEBUG_TERM_UUID");
        attributes.put("00000002" + COMMON_DEBUG_UUID_CHAR, "DEBUG_STDERR_UUID");

        attributes.put("00000001" + COMMON_CONFIG_UUID_CHAR, "REGISTERS_ACCESS_UUID");
        attributes.put("00000002" + COMMON_CONFIG_UUID_CHAR , "FEATURE_COMMAND_UUID");

        /**
         * This class define the characteristics associated with the features
         * There are 3 types of features:
         * <li>
         *     <ul>Base Feature: this feature has an uuid in the format  XXXXXXXX-0001-11e1-ac36-0002a5d5c51b,
         *     each bit of the first part tell witch feature is present inside the characteristics, and the presence
         *     of the feature is advertised inside the node feature mask field inside the advertise </ul>
         * </li>
         * @author STMicroelectronics - Central Labs.
         * MASK Temperature: 0x00040000
         * MASK Pressure: 0x00100000
         */
        attributes.put("00140000"+ BASE_FEATURE_COMMON_UUID, "Pressure + Temperature ");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
