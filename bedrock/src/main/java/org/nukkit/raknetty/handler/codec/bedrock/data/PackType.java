package org.nukkit.raknetty.handler.codec.bedrock.data;

// See also createPackTypeToStringMap, createStringToPackTypeMap
public enum PackType {

    // invalid
    INVALID,
    ADDON,
    RESERVED_2,
    RESERVED_3,
    // data, plugin, client_data, interface, javascript
    BEHAVIOR,
    // persona_piece
    PERSONA_PIECE,
    // resources, resourcepack
    RESOURCES,
    // skin_pack, skinpack
    SKIN_PACK,
    // world_template, worldtemplate
    WORLD_TEMPLATE;

    public static PackType valueOf(int index) {
        if (index < 0 || index > PackType.values().length) return null;
        return PackType.values()[index];
    }


}
