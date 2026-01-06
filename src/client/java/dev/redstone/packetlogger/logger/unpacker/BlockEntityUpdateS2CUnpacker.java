package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

/**
 * Unpacker für BlockEntityUpdateS2CPacket.
 * Zeigt Position, BlockEntity-Typ und komplettes NBT.
 */
public class BlockEntityUpdateS2CUnpacker implements PacketUnpacker<BlockEntityUpdateS2CPacket> {
    
    @Override
    public String unpack(BlockEntityUpdateS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Position
        BlockPos pos = packet.getPos();
        sb.append("pos:{x:").append(pos.getX())
          .append(",y:").append(pos.getY())
          .append(",z:").append(pos.getZ()).append("}");
        
        // BlockEntity Type
        try {
            String typeId = Registries.BLOCK_ENTITY_TYPE.getId(packet.getBlockEntityType()).toString();
            sb.append(",type:\"").append(typeId).append("\"");
        } catch (Exception e) {
            sb.append(",type:\"unknown\"");
        }
        
        // NBT Data
        NbtCompound nbt = packet.getNbt();
        if (nbt != null && !nbt.isEmpty()) {
            sb.append(",nbt:").append(nbt.asString());
        } else {
            sb.append(",nbt:{}");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
