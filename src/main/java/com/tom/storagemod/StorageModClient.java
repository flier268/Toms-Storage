package com.tom.storagemod;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.gui.GuiCraftingTerminal;
import com.tom.storagemod.gui.GuiStorageTerminal;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.tile.TileEntityPainted;

public class StorageModClient implements ClientModInitializer {
	protected static final Identifier PAINT = new Identifier(StorageMod.modid, "paint");

	@Override
	public void onInitializeClient() {
		ScreenRegistry.register(StorageMod.storageTerminal, GuiStorageTerminal::new);
		ScreenRegistry.register(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);

		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.paintedTrim, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCableFramed, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCablePainted, RenderLayer.getTranslucent());

		ClientSidePacketRegistry.INSTANCE.register(NetworkHandler.DATA_S2C, (ctx, buf) -> {
			CompoundTag tag = buf.readCompoundTag();
			ctx.getTaskQueue().submit(() -> {
				if(MinecraftClient.getInstance().currentScreen instanceof IDataReceiver) {
					((IDataReceiver)MinecraftClient.getInstance().currentScreen).receive(tag);
				}
			});
		});
		/*List<Block> lst = new ArrayList<>();
		lst.add(StorageMod.paintedTrim);
		lst.add(StorageMod.invCableFramed);
		//lst.add(StorageMod.invProxy);
		/*ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ModelVariantProvider() {

			@Override
			public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context)
					throws ModelProviderException {
				if(modelId.getNamespace().equals(StorageMod.modid)) {
					for (Block blockFor : lst) {
						Identifier baseLoc = Registry.BLOCK.getId(blockFor);
						if(blockFor.getStateManager().getStates().stream().filter(st -> st.method_28500(BlockInventoryCableFramed.PAINTED).orElse(true)).map(st -> BlockModels.getModelId(baseLoc, st)).
								anyMatch(r -> r.equals(modelId))) {
							return new BakedPaintedModel();
						}
					}
				}
				return null;
			}
		});*/
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ModelResourceProvider() {

			@Override
			public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context)
					throws ModelProviderException {
				if(resourceId.equals(PAINT)) {
					return new BakedPaintedModel();
				}
				return null;
			}
		});

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (world != null) {
				try {
					BlockState mimicBlock = ((TileEntityPainted)world.getBlockEntity(pos)).getPaintedBlockState();
					return MinecraftClient.getInstance().getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
				} catch (Exception var8) {
					return -1;
				}
			}
			return -1;
		}, StorageMod.paintedTrim, StorageMod.invCablePainted);
	}

	public static void tooltip(String key, List<Text> tooltip) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.translate("tooltip.toms_storage." + key).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new LiteralText(sp[i]));
			}
		} else {
			tooltip.add(new TranslatableText("tooltip.toms_storage.hold_shift_for_info").formatted(Formatting.ITALIC, Formatting.GRAY));
		}
	}

}