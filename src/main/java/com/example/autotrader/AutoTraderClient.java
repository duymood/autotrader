package com.example.autotrader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.lwjgl.glfw.GLFW;

/**
 * Mod client-side: khi bật (phím X), mỗi lần mở GUI giao dịch với dân làng,
 * mod sẽ tự tìm ô giao dịch "Sắt -> Ngọc Lục Bảo" và thực hiện liên tục
 * cho tới khi hết sắt hoặc dân làng hết lượt giao dịch, rồi tự đóng GUI.
 *
 * LƯU Ý: tên hàm/field của Minecraft (Yarn mappings) có thể thay đổi nhẹ
 * giữa các bản build. Nếu Gradle báo lỗi "cannot find symbol", xem phần
 * ghi chú cạnh dòng lỗi để biết hướng sửa (thường chỉ là đổi tên hàm).
 */
public class AutoTraderClient implements ClientModInitializer {

    // Bật/tắt tính năng tự động giao dịch. Mặc định tắt để tránh giao dịch ngoài ý muốn.
    public static boolean autoTradeEnabled = false;

    // Số lần tối đa thử click trong 1 lượt mở GUI (chống vòng lặp vô hạn nếu có lỗi logic).
    private static final int MAX_TRADE_ATTEMPTS = 128;

    private KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        // Đăng ký phím tắt, mặc định là phím X.
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autotrader.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.autotrader"
        ));

        // Kiểm tra phím được nhấn mỗi tick (cách chuẩn của Fabric để đọc keybinding).
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                autoTradeEnabled = !autoTradeEnabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("[AutoTrader] Tự động giao dịch: "
                                    + (autoTradeEnabled ? "BẬT" : "TẮT")),
                            true
                    );
                }
            }
        });

        // Khi màn hình giao dịch (MerchantScreen) vừa mở xong -> thử tự động trade.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (autoTradeEnabled && screen instanceof MerchantScreen merchantScreen) {
                autoTrade(client, merchantScreen);
            }
        });
    }

    private void autoTrade(MinecraftClient client, MerchantScreen screen) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        MerchantScreenHandler handler = screen.getScreenHandler();
        TradeOfferList offers = handler.getRecipes();

        int index = findIronToEmeraldOffer(offers);
        if (index == -1) {
            // Dân làng này không có kèo Sắt -> Ngọc Lục Bảo.
            return;
        }

        // Báo cho server biết ta đang chọn ô giao dịch số "index" (giống khi bấm chuột vào nó).
        handler.setSelectedRecipe(offers.get(index));
        client.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(index));

        int attempts = 0;
        TradeOffer offer = offers.get(index);
        int ironNeeded = offer.getOriginalFirstBuyItem().getCount();

        while (attempts < MAX_TRADE_ATTEMPTS
                && !offer.isDisabled()
                && countItem(client.player.getInventory(), Items.IRON_INGOT) >= ironNeeded) {

            // Slot 2 trong MerchantScreenHandler là ô kết quả (output) của giao dịch.
            // Dùng QUICK_MOVE (shift-click) để chuyển thẳng kết quả vào túi đồ, lặp lại mỗi lần 1 giao dịch.
            client.interactionManager.clickSlot(
                    handler.syncId,
                    2,
                    0,
                    SlotActionType.QUICK_MOVE,
                    client.player
            );

            attempts++;
        }

        // Xong thì tự đóng GUI cho gọn.
        client.player.closeHandledScreen();
    }

    private int findIronToEmeraldOffer(TradeOfferList offers) {
        for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = offers.get(i);
            ItemStack buy = offer.getOriginalFirstBuyItem();
            ItemStack sell = offer.getSellItem();

            if (buy.isOf(Items.IRON_INGOT) && sell.isOf(Items.EMERALD) && !offer.isDisabled()) {
                return i;
            }
        }
        return -1;
    }

    private int countItem(PlayerInventory inventory, net.minecraft.item.Item item) {
        int total = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
    }

