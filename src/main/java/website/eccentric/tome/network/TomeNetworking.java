package website.eccentric.tome.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import website.eccentric.tome.EccentricTome;

@EventBusSubscriber(modid = EccentricTome.ID)
public class TomeNetworking {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(EccentricTome.ID);
        registrar.playToServer(SelectBookPacket.TYPE, SelectBookPacket.STREAM_CODEC, SelectBookPacket::handle);
        registrar.playToServer(RevertToTomePacket.TYPE, RevertToTomePacket.STREAM_CODEC, RevertToTomePacket::handle);
    }
}
