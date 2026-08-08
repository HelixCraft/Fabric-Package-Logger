package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
*///?} else {
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
//?}

/**
 * Unpacker für SlotUpdateS2CPacket.
 * Zeigt Slot-ID und komplettes Item mit allen Components.
 */
public class SlotUpdateS2CUnpacker implements PacketUnpacker<
//? if >=26.1 {
    /*ClientboundContainerSetSlotPacket
*///?} else {
    ScreenHandlerSlotUpdateS2CPacket
//?}
> {

    @Override
    public String unpack(
//? if >=26.1 {
        /*ClientboundContainerSetSlotPacket packet
*///?} else {
        ScreenHandlerSlotUpdateS2CPacket packet
//?}
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("syncId:").append(
//? if >=26.1 {
            /*packet.getContainerId()
*///?} else {
            packet.getSyncId()
//?}
        );
        sb.append(",revision:").append(
//? if >=26.1 {
            /*packet.getStateId()
*///?} else {
            packet.getRevision()
//?}
        );
        sb.append(",slot:").append(packet.getSlot());
        
        ItemStack stack = /*? if >=26.1 { */ /*packet.getItem(); *//*?} else { */ packet.getStack(); /*?} */
        sb.append(",item:").append(ItemStackFormatter.format(stack));
        
        sb.append("}");
        return sb.toString();
    }
}