package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ChunkDeltaUpdateS2CPacket (MultiBlockChange).
 * Zeigt alle geänderten Blöcke mit Position und BlockState.
 */
public class ChunkDeltaUpdateS2CUnpacker implements PacketUnpacker<ChunkDeltaUpdateS2CPacket> {
    
    @Override
    public String unpack(ChunkDeltaUpdateS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Section Position via Reflection
        try {
            Object sectionPos = null;
            for (Field field : packet.getClass().getDeclaredFields()) {
                if (field.getType().getSimpleName().contains("ChunkSectionPos")) {
                    field.setAccessible(true);
                    sectionPos = field.get(packet);
                    break;
                }
            }
            
            if (sectionPos != null) {
                int x = (int) sectionPos.getClass().getMethod("getX").invoke(sectionPos);
                int y = (int) sectionPos.getClass().getMethod("getY").invoke(sectionPos);
                int z = (int) sectionPos.getClass().getMethod("getZ").invoke(sectionPos);
                sb.append("section:{x:").append(x).append(",y:").append(y).append(",z:").append(z).append("}");
            }
        } catch (Exception e) {
            sb.append("section:\"unknown\"");
        }
        
        // Alle Block-Änderungen via visitUpdates
        List<String> changes = new ArrayList<>();
        try {
            packet.visitUpdates((pos, state) -> {
                StringBuilder change = new StringBuilder();
                change.append("{pos:{x:").append(pos.getX())
                      .append(",y:").append(pos.getY())
                      .append(",z:").append(pos.getZ()).append("}");
                change.append(",state:").append(BlockUpdateS2CUnpacker.formatBlockState(state));
                change.append("}");
                changes.add(change.toString());
            });
        } catch (Exception e) {
            changes.add("{error:\"" + e.getMessage() + "\"}");
        }
        
        sb.append(",changes:[").append(String.join(",", changes)).append("]");
        sb.append(",count:").append(changes.size());
        
        sb.append("}");
        return sb.toString();
    }
}
