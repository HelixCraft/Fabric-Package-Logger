package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClientboundBlockUpdatePacket.
 * Zeigt Position und kompletten BlockState mit allen Properties.
 */
public class BlockUpdateS2CUnpacker implements PacketUnpacker<ClientboundBlockUpdatePacket> {

    @Override
    public String unpack(ClientboundBlockUpdatePacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Position
        BlockPos pos = packet.getPos();
        sb.append("pos:{x:").append(pos.getX())
          .append(",y:").append(pos.getY())
          .append(",z:").append(pos.getZ()).append("}");

        // BlockState
        BlockState state = packet.getBlockState();
        sb.append(",state:").append(formatBlockState(state));

        sb.append("}");
        return sb.toString();
    }

    public static String formatBlockState(BlockState state) {
        if (state == null) return "null";

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Block ID
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        sb.append("block:\"").append(blockId).append("\"");

        // Properties
        if (!state.getProperties().isEmpty()) {
            sb.append(",properties:{");
            List<String> props = new ArrayList<>();
            for (Property<?> property : state.getProperties()) {
                String value = getPropertyValueString(state, property);
                props.add(property.getName() + ":\"" + value + "\"");
            }
            sb.append(String.join(",", props));
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private static <T extends Comparable<T>> String getPropertyValueString(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
