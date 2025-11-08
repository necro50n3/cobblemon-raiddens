package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class AbstractReloadImpl {
    protected final String path;
    protected final String idRemove;
    protected final DataType type;

    protected AbstractReloadImpl(String path, DataType suffix) {
        this(path, path + "/", suffix);
    }

    protected AbstractReloadImpl(String path, String idRemove, DataType suffix) {
        this.path = path;
        this.idRemove = idRemove;
        this.type = suffix;
    }

    public void load(@NotNull ResourceManager manager) {
        this.preLoad();

        manager.listResources(this.path, path -> path.toString().endsWith(this.suffix())).forEach((id, resource) -> {
            try (InputStream input = resource.open()) {
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(this.idRemove, "").replace(this.suffix(), ""));
                if (this.type == DataType.JSON) this.loadJson(input, key);
                else if (this.type == DataType.NBT) this.loadNbt(input, key);
            } catch (Exception e) {
                this.onError(id, e);
            }
        });

        this.postLoad();
    }

    protected String suffix() {
        return this.type.suffix();
    }

    protected void loadJson(InputStream input, ResourceLocation key) {
        JsonObject object = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        this.onLoad(key, object);
    }

    protected void loadNbt(InputStream input, ResourceLocation key) throws IOException {
        CompoundTag nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
        this.onLoad(key, nbt);
    }

    protected abstract void preLoad();
    protected void onLoad(ResourceLocation key, JsonObject object) {}
    protected void onLoad(ResourceLocation key, CompoundTag nbt) {}
    protected abstract void onError(ResourceLocation id, Exception e);
    protected abstract void postLoad();

    protected enum DataType {
        JSON(".json"),
        NBT(".nbt");

        private final String suffix;

        DataType(String suffix) {
            this.suffix = suffix;
        }

        private String suffix() {
            return this.suffix;
        }
    }
}
