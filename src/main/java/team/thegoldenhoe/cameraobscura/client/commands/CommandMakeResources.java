package team.thegoldenhoe.cameraobscura.client.commands;

import com.google.common.collect.BiMap;
import com.google.gson.*;
import com.mia.craftstudio.CSModel.ModelNode;
import com.mia.craftstudio.CSPack;
import com.mia.craftstudio.CSPack.ProjectEntry;
import com.mia.craftstudio.CraftStudioLib;
import com.mia.craftstudio.minecraft.CraftStudioRendererVBO;
import com.mia.craftstudio.minecraft.ModelMetadata;
import com.mia.craftstudio.minecraft.client.CSClientModelWrapperVBO;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import team.thegoldenhoe.cameraobscura.CSModelMetadata;
import team.thegoldenhoe.cameraobscura.Info;
import team.thegoldenhoe.cameraobscura.TabProps;
import team.thegoldenhoe.cameraobscura.utils.ModelHandler;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class CommandMakeResources extends CommandBase {
    File decoOutputDir = new File("DecocraftGeneratedResources");

    @Override
    public String getName() {
        return "decomakeres";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/decomakeres [texpack|json|list]";
    }

    private Color averageColorFromArray(int[] pixels) {
        int addCount = 0, totalR = 0, totalG = 0, totalB = 0;
        for (int pixel : pixels) {
            if (((pixel >> 24) & 0xFF) != 0) {
                addCount++;
                Color c = new Color(pixel);
                totalR += c.getRed();
                totalG += c.getGreen();
                totalB += c.getBlue();
            }
        }
        if (addCount > 0) {
            totalR /= addCount;
            totalG /= addCount;
            totalB /= addCount;
        }
        return new Color(totalR, totalG, totalB);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString("Please choose a resource to export: texturepack, json, list, lang, or all."));
            return;
        }

        String command = args[0].toLowerCase();
        if ("texturepack".equals(command)) {
            makeTexturePack(sender);
        } else if ("dumpcolors".equals(command)) {
            Field[] blockFieldList = Blocks.class.getDeclaredFields();
            ArrayList<Block> mcBlockList = new ArrayList<Block>();
            for (Field bf : blockFieldList) {
                try {
                    mcBlockList.add((Block) (bf.get(null)));
                } catch (Throwable t) {
                }
            }
            //ArrayList<String, Integer> uniqueColors;
            HashSet<String> existingColors = new HashSet<String>();
            HashSet<String> existingNames = new HashSet<String>();
            HashSet<String> conflictingNames = new HashSet<String>();
            ArrayList<ItemStack> validBlocks = new ArrayList<ItemStack>();
            String output = "Calculated Vanilla MC colors:\r";
            for (Block b : mcBlockList) {
                try {
                    Item bItem = Item.getItemFromBlock(b);
                    NonNullList<ItemStack> subList = NonNullList.create();

                    if (bItem != null) {
                        if (bItem.getHasSubtypes()) {
                            try {
                                b.getSubBlocks(b.getCreativeTabToDisplayOn(), subList);
                            } catch (Throwable t) {
                            }
                        } else {
                            try {
                                for (int i = 0, end = (bItem.isDamageable() ? bItem.getMaxDamage() : 0); i <= end; i++) {
                                    subList.add(new ItemStack(b, 1, i));
                                }
                            } catch (Throwable t) {
                            }
                        }
                    }

                    // Pass one: filter out duplicate icon texture names and buiild list of target blocks, also build name set to remove conflicts(ie: mushrooms)
                    for (ItemStack bStack : subList) {
                        try {
                            //IIcon ic = b.getIcon(1, bStack.getItemDamage());
                            TextureAtlasSprite ic = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(b.getStateFromMeta(bStack.getItemDamage()));
                            if (existingColors.contains(ic.getIconName().intern())) {
                                continue;
                            }
                            existingColors.add(ic.getIconName().intern());
                            if (existingNames.contains(bStack.getDisplayName())) {
                                conflictingNames.add(bStack.getDisplayName());
                            } else {
                                existingNames.add(bStack.getDisplayName());
                            }
                            validBlocks.add(bStack);
                        } catch (Throwable t) {
                        }
                    }
                } catch (Throwable t) {
                }
            }
            // Pass two: determine average block color and best matching name
            for (ItemStack bStack : validBlocks) {
                try {
                    String name = bStack.getDisplayName();
                    //IIcon ic = Block.getBlockFromItem(bStack.getItem()).getIcon(1, bStack.getItemDamage());
                    TextureAtlasSprite ic = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Block.getBlockFromItem(bStack.getItem()).getStateFromMeta(bStack.getItemDamage()));

                    if (conflictingNames.contains(name)) {
                        name = "_" + ic.getIconName().replaceAll("_top|_bottom|_side|_base|_stage_[0-9]|_layer_[0-9]|_colored|_inside", "");
                    }
                    name = name.replaceAll("\\W", "");

                    StringBuilder nameBuilder = new StringBuilder();
                    for (char c : name.toCharArray()) {
                        if (Character.isUpperCase(c)) {
                            nameBuilder.append('_');
                            nameBuilder.append(Character.toLowerCase(c));
                        } else {
                            nameBuilder.append(c);
                        }
                    }
                    // now fix some derps...
                    name = nameBuilder.toString().replaceAll("blockof", "block").replaceAll("t_n_t", "tnt").replaceAll("chiseled_quartz_block", "quartz");

                    ResourceLocation resourcelocation = new ResourceLocation(ic.getIconName());
                    ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", new Object[]{"textures", resourcelocation.getResourcePath(), ".png"}));
                    Color ac = averageColorFromArray(TextureUtil.readImageData(Minecraft.getMinecraft().getResourceManager(), resourcelocation1));
                    // TODO : Get the block color another way since Block.getBlockColor() doesn't exists anymore
//					Color bColor = new Color(Block.getBlockFromItem(bStack.getItem()).getBlockColor());
//					String hexColor = Integer.toHexString((new Color((ac.getRed()*bColor.getRed())/65025f, (ac.getGreen()*bColor.getGreen())/65025f, (ac.getBlue()*bColor.getBlue())/65025f)).getRGB()).toUpperCase().substring(2);
//					output += String.format("mc%s(0x%s),", name, hexColor) + "\r";
                } catch (Throwable t) {
                }
            }
            System.out.println(validBlocks.size() + " " + output);
        } else if ("json".equals(command)) {
            boolean forceAdd = false;
            boolean updateScale = false;
            boolean updateTabs = false;
            boolean updateNames = false;
            if (args.length >= 2) {
                for (int i = 1; i < args.length; i++) {
                    if ("forceadd".equals(args[i].toLowerCase())) {
                        forceAdd = true;
                    } else if ("updatescale".equals(args[i].toLowerCase())) {
                        updateScale = true;
                    } else if ("updatetabs".equals(args[i].toLowerCase())) {
                        updateTabs = true;
                    } else if ("updatenames".equals(args[i].toLowerCase())) {
                        updateNames = true;
                    }
                }
            }
            makeJSON(sender, forceAdd, updateScale, updateTabs, updateNames);
        } else if ("list".equals(command)) {
            makeMasterList(sender);
        } else if ("lang".equals(command)) {
            makeLanguage(sender);
        } else if ("all".equals(command)) {
            makeJSON(sender, true, false, false, false);
            makeMasterList(sender);
            makeTexturePack(sender);
            makeLanguage(sender);
        } else {
            sender.sendMessage(new TextComponentString("Unknown subcommand: " + args[0]));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private class DecoMetadataJsonSerializer implements JsonSerializer<CSModelMetadata> {
        private CSModelMetadata defaultMeta = new CSModelMetadata();
        private List<Field> fieldsToTrim = new ArrayList<Field>();

        private Gson gson = new GsonBuilder().serializeNulls().create();

        public DecoMetadataJsonSerializer() {
            for (Field field : CSModelMetadata.class.getFields()) {
                if ("decocraftModelID".equals(field.getName())) {
                    // Ensure that the ID is always exported for consistency
                    continue;
                }
                if (!Modifier.isTransient(field.getModifiers())) {
                    fieldsToTrim.add(field);
                }
            }
        }

        @Override
        public JsonElement serialize(CSModelMetadata src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject trimmedJsonObject = (JsonObject) gson.toJsonTree(src);

            for (Field field : fieldsToTrim) {
                try {
                    Object value = field.get(src);
                    if (value == null) {
                        // to fix variant null overrides, had to enable serializing nulls, so strip top level nulls here
                        trimmedJsonObject.remove(field.getName());
                    }
                    if (value != null && value.equals(field.get(defaultMeta))) {
                        trimmedJsonObject.remove(field.getName());
                    }
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }

            trimmedJsonObject.remove("recipe");

            // This isn't needed, and just visually clutters up the Json
            if (src.getDefaultTextureName().equals(src.textureOverride)) {
                trimmedJsonObject.remove("textureOverride");
            }

            return trimmedJsonObject;
        }
    }

    private void makeTexturePack(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Exporting default texturepack for skin creation."));
        try {
            decoOutputDir.mkdirs();

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(decoOutputDir, "Decocraft_Template_TexturePack.zip")));

            zipOut.putNextEntry(new ZipEntry("pack.meta"));
            zipOut.write("{\n  \"pack\": {\n    \"pack_format\": 1,\n    \"description\": \"Decocraft default texture pack!\"\n  }\n}".getBytes());
            zipOut.closeEntry();
            String path = "assets/";
            zipOut.putNextEntry(new ZipEntry(path));
            zipOut.closeEntry();
            path += Info.MODID.toLowerCase() + "/";
            zipOut.putNextEntry(new ZipEntry(path));
            zipOut.closeEntry();
            path += "textures/";
            zipOut.putNextEntry(new ZipEntry(path));
            zipOut.closeEntry();
            path += "models/";
            zipOut.putNextEntry(new ZipEntry(path));
            zipOut.closeEntry();

            List<CSModelMetadata> modelList = new ArrayList(ModelHandler.modelData.values());

            Collections.sort(modelList);

            for (CSModelMetadata metadata : modelList) {
                try {
                    zipOut.putNextEntry(new ZipEntry(path + metadata.textureOverride));
                    zipOut.write(metadata.csmodel.getRawTexture());
                    zipOut.closeEntry();
                } catch (ZipException ze) {
                    // Duplicate entries will throw this, some models reuse the same texture name by default
                }
            }

            zipOut.close();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNumber(String str) {
        boolean foundDecimal = false;
        for (char c : str.toCharArray()) {
            if (c == '.') {
                if (foundDecimal) {
                    // second decimal point?!
                    return false;
                }
                foundDecimal = true;
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private void makeJSON(ICommandSender sender, boolean forceAdd, boolean updateScale, boolean updateTabs, boolean updateName) {
        sender.sendMessage(new TextComponentString("Creating json descriptor files for packs." + (forceAdd ? " !! Adding new models to live Model Metadata !!" : "")));

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CSModelMetadata.class, new DecoMetadataJsonSerializer());
        builder.setPrettyPrinting();
        builder.serializeNulls();
        Gson gson = builder.create();

        try {
            decoOutputDir.mkdirs();

            List<CSPack> packs = ModelHandler.csproject.getPacks();

            for (CSPack pack : packs) {
                List<ModelMetadata> packModelData = new ArrayList<ModelMetadata>();
                BiMap<Integer, ProjectEntry> packentries = pack.getEntries();

                for (Entry<Integer, ProjectEntry> entry : packentries.entrySet()) {
                    if (entry.getValue().getType() != CraftStudioLib.type_model) {
                        // Skip over non models
                        continue;
                    }

                    String craftstudioAssetName = entry.getValue().getName();
                    String[] nameParts = craftstudioAssetName.split("_", 3);

                    if (entry.getValue().isFolder() || !(isNumber(nameParts[0]) || "item".equals(nameParts[0].toLowerCase()))) {
                        //skip folders and models that do not have a deco id number set or are a deco item
                        continue;
                    }

                    JsonElement tempElement = ModelHandler.csproject.getDescriptor(entry.getKey());

                    int decocraftID = 0;
                    float modelScale = 1f;
                    String decocraftName = "";
                    TabProps decocraftCreativeTab = TabProps.Main;
                    boolean basicMeta = false;

                    // New model, let's do some parsing on the model name and figure out what is going on
                    if (tempElement == null || updateScale || updateTabs || updateName) {
                        if (nameParts.length > 1) {
                            if ("item".equals(nameParts[0].toLowerCase())) {
                                // Items parse and use standard modelmetadata, not the specific extra deco meta used for making deco specific tiles
                                basicMeta = true;
                            } else {
                                try {
                                    decocraftID = Integer.valueOf(nameParts[0]);
                                    if (nameParts.length == 3) {
                                        boolean isName = true;

                                        if (isNumber(nameParts[1])) {
                                            modelScale = Float.valueOf(nameParts[1]);
                                            decocraftName = nameParts[2];
                                            isName = false;
                                        } else if (nameParts[1].startsWith("@")) {
                                            String[] numParts = nameParts[1].substring(1).split(";");
                                            int constantAnim = Integer.parseInt(numParts[0]);
                                            if (numParts.length > 1) {
                                                modelScale = Float.valueOf(numParts[1]);
                                            }
                                            decocraftName = nameParts[2];
                                            isName = false;
                                        }

                                        if (isName) {
                                            decocraftName = String.format("%s_%s", nameParts[1], nameParts[2]);
                                        }
                                    } else {
                                        decocraftName = nameParts[1];
                                    }
                                } catch (NumberFormatException nfe) {
                                    sender.sendMessage(new TextComponentString(String.format("Model[%] is not an 'Item_' or formatted with a proper DecoCraft 'IDNumber_Decocraftname' format.", craftstudioAssetName)));
                                }
                            }
                        } else {
                            sender.sendMessage(new TextComponentString(String.format("Model[%] is not an 'Item_' or formatted with a proper DecoCraft 'IDNumber_Decocraftname' format.", craftstudioAssetName)));
                            // valid models to parse are formatted at least as [IDNUM|Item]_NAME, so this should be 2+ for valid models, anything else wont parse
                            continue;
                        }

                        try {
                            decocraftCreativeTab = TabProps.valueOf(packentries.get((int) entry.getValue().getParentID()).getName());
                        } catch (Exception e) {
                            // dont care of the error type, if a name doesn't match, then jvm rolls back to TabProps.Main
                        }
                    }

                    if (!basicMeta) {
                        CSModelMetadata tempMeta;
                        if (tempElement != null) {
                            // copy over existing metadata if it exists
                            tempMeta = ModelHandler.modelData.get(tempElement.getAsJsonObject().get("decocraftModelID").getAsInt());

                            tempMeta.craftstudioAssetName = craftstudioAssetName;
                            if (updateScale) {
                                tempMeta.scale = modelScale;
                            }
                            if (updateTabs) {
                                tempMeta.tab = decocraftCreativeTab;
                            }
                            if (updateName && (tempMeta.variants == null || tempMeta.variants.isEmpty())) {
                                // only force update names for non Variation models. Variants are likely handling themselves
                                tempMeta.name = decocraftName;
                            }
                        } else {
                            tempMeta = new CSModelMetadata();

                            tempMeta.craftstudioAssetName = craftstudioAssetName;
                            tempMeta.craftstudioAssetID = entry.getKey();
                            tempMeta.decocraftModelID = decocraftID;
                            tempMeta.scale = modelScale;
                            tempMeta.name = decocraftName;
                            tempMeta.csmodel = ModelHandler.csproject.getModel((int) entry.getValue().getID());
                            tempMeta.textureOverride = tempMeta.getDefaultTextureName();
                            tempMeta.tab = decocraftCreativeTab;
                        }
                        if (forceAdd) {
                            if (tempMeta.wrapper == null) {
                                tempMeta.wrapper = new CSClientModelWrapperVBO(tempMeta);
                            }
                            ((CSClientModelWrapperVBO) tempMeta.wrapper).deleteGlTexture();
                            TextureUtil.uploadTextureImage(((CSClientModelWrapperVBO) tempMeta.wrapper).getGlTextureId(), tempMeta.csmodel.getTexture());
                            if (((CSClientModelWrapperVBO) tempMeta.wrapper).topRenderers.size() == 0) {
                                for (ModelNode node : tempMeta.csmodel.getTopNodes()) {
                                    ((CSClientModelWrapperVBO) tempMeta.wrapper).addRenderer(new CraftStudioRendererVBO(tempMeta.wrapper.nodeCache.get(node)));
                                }
                            }
                            ModelHandler.modelData.put(tempMeta.decocraftModelID, tempMeta);
                        }

                        packModelData.add(tempMeta);
                    } else {
                        // Since this is only reading in basic meta information, there is no real need to copy over existing data
                        ModelMetadata tempMeta = new ModelMetadata();

                        tempMeta.craftstudioAssetName = craftstudioAssetName;
                        tempMeta.craftstudioAssetID = entry.getKey();

                        if (isNumber(nameParts[1])) {
                            tempMeta.scale = Float.valueOf(nameParts[1]);
                        }

                        if ("decowand".equals(nameParts[nameParts.length - 1].toLowerCase())) {
                            // skipping creation of metadata for the decowand, we're loading it directly in the main metadata loading method
                            continue;
                        }
                        packModelData.add(tempMeta);
                    }
                }

                Collections.sort(packModelData);

                // Scan new metaData collection for known issues
                for (ModelMetadata meta : packModelData) {
                    String id = meta.craftstudioAssetName.split("_", 3)[0];
                    if (!("item".equals(id.toLowerCase()) || (isNumber(id) && id.length() == 4))) {
                        sender.sendMessage(new TextComponentString(String.format("Identifier[%s] for Model[%s] is not 'Item' or a four digit number.", id, meta.craftstudioAssetName)));
                    }
                    if (meta.csmodel.getRootNode() == null) {
                        sender.sendMessage(new TextComponentString(String.format("Model[%s] does not have a properly named 'RootNode'.", meta.craftstudioAssetName)));
                    }
                }

                File jsonFile = new File(decoOutputDir, pack.getName().replace(".cspack", ".json"));
                jsonFile.setWritable(true);
                PrintWriter fout = new PrintWriter(jsonFile);
                fout.print(gson.toJson(packModelData));
                fout.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeMasterList(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Exporting master model CSV file."));
        try {
            decoOutputDir.mkdirs();

            File masterListFile = new File(decoOutputDir, "Decocraft_Master_Model_list.csv");
            masterListFile.setWritable(true);
            PrintWriter fout = new PrintWriter(masterListFile);

            fout.print("DecoID,Tab,ModelName\n");

            List<CSModelMetadata> modelList = new ArrayList(ModelHandler.modelData.values());

            Collections.sort(modelList);

            for (CSModelMetadata metadata : modelList) {
                fout.print(String.format("%d,%s,%s\n", metadata.decocraftModelID, metadata.tab.name(), metadata.name));
            }

            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeLanguage(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Updating and exporting en_us.lang"));
        try {
            decoOutputDir.mkdirs();

            File newLangFile = new File(decoOutputDir, "en_us.lang");
            newLangFile.setWritable(true);

            //noinspection Since15
            Files.copy(CommandMakeResources.class.getResourceAsStream("/assets/cameraobscura/lang/en_us.lang"), newLangFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(newLangFile, true)));

            List<CSModelMetadata> modelList = new ArrayList(ModelHandler.modelData.values());
            Collections.sort(modelList);

            for (CSModelMetadata metadata : modelList) {
                String unlocalizedName = "item.cameraobscura." + ModelHandler.modelData.get(metadata.decocraftModelID).name.toLowerCase().replace(" ", "_").replace("'", "_") + ".name";
                String localizedName = "" + I18n.translateToLocal(unlocalizedName).trim();
                if (localizedName.startsWith("item.cameraobscura.")) {
                    fout.print(String.format("%s=%s\n", unlocalizedName, metadata.name));
                }
            }

            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
