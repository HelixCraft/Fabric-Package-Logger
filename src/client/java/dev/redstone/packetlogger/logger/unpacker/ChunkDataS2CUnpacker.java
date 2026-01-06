package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ChunkDataS2CPacket.
 * Loggt nicht den rohen Buffer, aber alle BlockEntity-Daten mit NBT.
 */
public class ChunkDataS2CUnpacker implements PacketUnpacker<ChunkDataS2CPacket> {
    
    @Override
    public String unpack(ChunkDataS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Chunk Position
        sb.append("chunkX:").append(packet.getChunkX());
        sb.append(",chunkZ:").append(packet.getChunkZ());
        
        try {
            // Chunk Data via Reflection (API kann sich ändern)
            Object chunkData = packet.getChunkData();
            if (chunkData != null) {
                // Heightmaps
                try {
                    Method heightmapMethod = chunkData.getClass().getMethod("heightmap");
                    NbtCompound heightmaps = (NbtCompound) heightmapMethod.invoke(chunkData);
                    if (heightmaps != null && !heightmaps.isEmpty()) {
                        sb.append(",heightmapKeys:[");
                        List<String> keys = new ArrayList<>();
                        for (String key : heightmaps.getKeys()) {
                            keys.add("\"" + key + "\"");
                        }
                        sb.append(String.join(",", keys));
                        sb.append("]");
                    }
                } catch (Exception e) {
                    // Ignore
                }
                
                // Section Data Size
                try {
                    Method sectionsMethod = chunkData.getClass().getMethod("sectionsData");
                    byte[] sectionData = (byte[]) sectionsMethod.invoke(chunkData);
                    sb.append(",sectionDataSize:").append(sectionData != null ? sectionData.length : 0);
                } catch (Exception e) {
                    // Ignore
                }
                
                // Block Entities via Reflection
                try {
                    Method beMethod = chunkData.getClass().getMethod("blockEntities");
                    List<?> blockEntities = (List<?>) beMethod.invoke(chunkData);
                    if (blockEntities != null && !blockEntities.isEmpty()) {
                        sb.append(",blockEntities:[");
                        List<String> entities = new ArrayList<>();
                        for (Object beData : blockEntities) {
                            entities.add(formatBlockEntityData(beData, packet.getChunkX(), packet.getChunkZ()));
                        }
                        sb.append(String.join(",", entities));
                        sb.append("]");
                        sb.append(",blockEntityCount:").append(blockEntities.size());
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        } catch (Exception e) {
            sb.append(",error:\"").append(e.getMessage()).append("\"");
        }
        
        // Light Data vorhanden?
        sb.append(",hasLightData:").append(packet.getLightData() != null);
        
        sb.append("}");
        return sb.toString();
    }
    
    private String formatBlockEntityData(Object beData, int chunkX, int chunkZ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        try {
            Class<?> clazz = beData.getClass();
            
            // Position
            try {
                Method localXzMethod = clazz.getMethod("localXz");
                Method yMethod = clazz.getMethod("y");
                
                int localXz = (int) localXzMethod.invoke(beData);
                int y = (int) yMethod.invoke(beData);
                
                int localX = localXz >> 4;
                int localZ = localXz & 15;
                int absX = chunkX * 16 + localX;
                int absZ = chunkZ * 16 + localZ;
                
                sb.append("pos:{x:").append(absX)
                  .append(",y:").append(y)
                  .append(",z:").append(absZ).append("}");
            } catch (Exception e) {
                sb.append("pos:\"unknown\"");
            }
            
            // Type
            try {
                Method typeMethod = clazz.getMethod("type");
                Object type = typeMethod.invoke(beData);
                if (type != null) {
                    sb.append(",type:\"").append(type.toString()).append("\"");
                }
            } catch (Exception e) {
                // Ignore
            }
            
            // NBT
            try {
                Method nbtMethod = clazz.getMethod("nbt");
                NbtCompound nbt = (NbtCompound) nbtMethod.invoke(beData);
                if (nbt != null && !nbt.isEmpty()) {
                    sb.append(",nbt:").append(nbt.asString());
                }
            } catch (Exception e) {
                // Ignore
            }
            
        } catch (Exception e) {
            sb.append("error:\"").append(e.getMessage()).append("\"");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
