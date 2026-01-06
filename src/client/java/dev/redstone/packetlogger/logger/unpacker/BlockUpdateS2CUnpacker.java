package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für BlockUpdateS2CPacket.
 * Zeigt Position und kompletten BlockState mit allen Properties.
 */
public class BlockUpdateS2CUnpacker implements PacketUnpacker<BlockUpdateS2CPacket> {
    
    @Override
    public String unpack(BlockUpdateS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Position
        BlockPos pos = packet.getPos();
        sb.append("pos:{x:").append(pos.getX())
          .append(",y:").append(pos.getY())
          .append(",z:").append(pos.getZ()).append("}");
        
        // BlockState
        BlockState state = packet.getState();
        sb.append(",state:").append(formatBlockState(state));
        
        sb.append("}");
        return sb.toString();
    }
    
    public static String formatBlockState(BlockState state) {
        if (state == null) return "null";
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Block ID
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
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
    
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyValueString(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }
}
