package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unpacker für ChunkDataS2CPacket.
 * Loggt nicht den rohen Buffer, aber alle BlockEntity-Daten mit NBT.
 */
public class ChunkDataS2CUnpacker implements PacketUnpacker<ChunkDataS2CPacket> {
    
    @Override
    public String unpack(ChunkDataS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("chunkX:").append(packet.getChunkX());
        sb.append(",chunkZ:").append(packet.getChunkZ());

        try {
            Object chunkData = packet.getChunkData();
            if (chunkData != null) {
                appendChunkDataDetails(sb, packet);
            }
        } catch (Exception e) {
            sb.append(",error:\"").append(TextFormatter.escapeString(e.getMessage())).append("\"");
        }

        appendLightData(sb, packet.getLightData());
        sb.append("}");
        return sb.toString();
    }

    private void appendChunkDataDetails(StringBuilder sb, ChunkDataS2CPacket packet) {
        var chunkData = packet.getChunkData();

        Map<?, ?> heightmap = chunkData.getHeightmap();
        if (heightmap != null && !heightmap.isEmpty()) {
            sb.append(",heightmapKeys:[");
            List<String> keys = new ArrayList<>();
            for (Object key : heightmap.keySet()) {
                keys.add(TextFormatter.formatPlainString(String.valueOf(key)));
            }
            sb.append(String.join(",", keys));
            sb.append("]");
        }

        PacketByteBuf sectionsData = chunkData.getSectionsDataBuf();
        sb.append(",sectionDataSize:").append(sectionsData.readableBytes());

        List<String> blockEntities = new ArrayList<>();
        chunkData.getBlockEntities(packet.getChunkX(), packet.getChunkZ()).accept((pos, type, nbt) -> {
            blockEntities.add(formatBlockEntityData(pos, type, nbt));
        });

        sb.append(",blockEntityCount:").append(blockEntities.size());
        if (!blockEntities.isEmpty()) {
            sb.append(",blockEntities:[").append(String.join(",", blockEntities)).append("]");
        }
    }

    private void appendLightData(StringBuilder sb, LightData lightData) {
        sb.append(",hasLightData:").append(lightData != null);
        if (lightData == null) {
            return;
        }

        sb.append(",skyLightUpdates:").append(lightData.getSkyNibbles().size());
        sb.append(",blockLightUpdates:").append(lightData.getBlockNibbles().size());
        sb.append(",initializedSkySections:").append(lightData.getInitedSky().cardinality());
        sb.append(",initializedBlockSections:").append(lightData.getInitedBlock().cardinality());
    }

    private String formatBlockEntityData(net.minecraft.util.math.BlockPos pos, net.minecraft.block.entity.BlockEntityType<?> type, net.minecraft.nbt.NbtCompound nbt) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("pos:{x:").append(pos.getX())
                .append(",y:").append(pos.getY())
                .append(",z:").append(pos.getZ()).append("}");

        if (type != null) {
            sb.append(",type:\"").append(Registries.BLOCK_ENTITY_TYPE.getId(type)).append("\"");
        }

        if (nbt != null && !nbt.isEmpty()) {
            sb.append(",nbt:").append(nbt.asString().orElse(nbt.toString()));
        }

        sb.append("}");
        return sb.toString();
    }
}
