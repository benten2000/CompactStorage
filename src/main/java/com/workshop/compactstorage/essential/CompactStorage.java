package com.workshop.compactstorage.essential;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.workshop.compactchests.CompactChests;
import com.workshop.compactchests.init.ChestBlocks;
import com.workshop.compactchests.init.ChestItems;
import com.workshop.compactstorage.creativetabs.CreativeTabCompactStorage;
import com.workshop.compactstorage.essential.handler.ConfigurationHandler;
import com.workshop.compactstorage.essential.handler.FirstTimeRunHandler;
import com.workshop.compactstorage.essential.handler.GuiHandler;
import com.workshop.compactstorage.essential.init.StorageBlocks;
import com.workshop.compactstorage.essential.init.StorageInfo;
import com.workshop.compactstorage.essential.init.StorageItems;
import com.workshop.compactstorage.essential.proxy.IProxy;
import com.workshop.compactstorage.network.handler.C01HandlerUpdateBuilder;
import com.workshop.compactstorage.network.handler.C02HandlerCraftChest;
import com.workshop.compactstorage.network.packet.C01PacketUpdateBuilder;
import com.workshop.compactstorage.network.packet.C02PacketCraftChest;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * Created by Toby on 06/11/2014.
 */
@Mod(modid = StorageInfo.ID, name = StorageInfo.NAME, version = StorageInfo.VERSION)
public class CompactStorage
{
    @Instance(StorageInfo.ID)
    public static CompactStorage instance;

    public static CompactChests legacy_instance;

    @SidedProxy(clientSide = StorageInfo.CLIENT_PROXY, serverSide = StorageInfo.SERVER_PROXY, modId = StorageInfo.ID)
    public static IProxy proxy;

    public static CreativeTabs tabCS;

    public static final Logger logger = LogManager.getLogger("CompactStorage");
    public static boolean deobf;

    public SimpleNetworkWrapper wrapper;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        try
        {
            deobf = true; //Class.forName("net.minecraft.world.World") == null ? false : true;
        }
        catch(Exception ex)
        {
            logger.warn("Could not set deobf variable. Assuming normal game.");
        }

        logger.info("Are we in deofb? " + (deobf ? "Yep!" : "Nope, going retro!"));

        legacy_instance = new CompactChests();
        
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(StorageInfo.ID);
        wrapper.registerMessage(C01HandlerUpdateBuilder.class, C01PacketUpdateBuilder.class, 0, Side.SERVER);
        wrapper.registerMessage(C02HandlerCraftChest.class, C02PacketCraftChest.class, 1, Side.SERVER);
        
        switch(FMLCommonHandler.instance().getEffectiveSide())
        {
            case CLIENT: legacy_instance.proxy = new com.workshop.compactchests.proxy.Client(); break;
            case SERVER: legacy_instance.proxy = new com.workshop.compactchests.proxy.Server(); break;
        }

        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        tabCS = new CreativeTabCompactStorage();

        ChestBlocks.init();
        StorageBlocks.init();

        ChestItems.init();
        StorageItems.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if(deobf)
        {
            NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        }
        else
        {
            NetworkRegistry.INSTANCE.registerGuiHandler(instance, new com.workshop.compactchests.client.GuiHandler());


            GameRegistry.addShapedRecipe(new ItemStack(ChestBlocks.doubleChest), "SIS", "ICI", "SIS", 'S', new ItemStack(Items.stick, 1), 'I', new ItemStack(Items.iron_ingot, 1), 'C', new ItemStack(Blocks.chest, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestBlocks.tripleChest), "SIS", "ICI", "SIS", 'S', new ItemStack(Blocks.cobblestone_wall, 1), 'I', new ItemStack(Items.iron_ingot, 1), 'C', new ItemStack(ChestBlocks.doubleChest, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestBlocks.quadrupleChest), "SIS", "ICI", "SIS", 'S', new ItemStack(Blocks.cobblestone_wall, 1), 'I', new ItemStack(Items.gold_ingot, 1), 'C', new ItemStack(ChestBlocks.tripleChest, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestBlocks.quintupleChest), "SIS", "ICI", "SIS", 'S', new ItemStack(Blocks.glass_pane, 1), 'I', new ItemStack(Items.gold_ingot, 1), 'C', new ItemStack(ChestBlocks.quadrupleChest, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestBlocks.sextupleChest), "SIS", "ICI", "SIS", 'S', new ItemStack(Blocks.glass_pane, 1), 'I', new ItemStack(Items.diamond, 1), 'C', new ItemStack(ChestBlocks.quintupleChest, 1));
        
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.single_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(Blocks.chest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.double_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(ChestBlocks.doubleChest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.triple_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(ChestBlocks.tripleChest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.quadruple_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(ChestBlocks.quadrupleChest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.quintuple_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(ChestBlocks.quintupleChest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
            GameRegistry.addShapedRecipe(new ItemStack(ChestItems.sextuple_backpack), "WSW", "SCS", "WSW", 'C', new ItemStack(ChestBlocks.sextupleChest, 1), 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'S', new ItemStack(Items.string, 1));
        }

        proxy.registerRenderers();
        legacy_instance.postInitialization(event);
        
        MinecraftForge.EVENT_BUS.register(new FirstTimeRunHandler());
        FMLCommonHandler.instance().bus().register(new FirstTimeRunHandler());
    }
}
