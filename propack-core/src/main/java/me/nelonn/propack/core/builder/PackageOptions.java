package me.nelonn.propack.core.builder;

public class PackageOptions {
    public final int compressionLevel;
    public final boolean protection;
    public final String comment;

    public PackageOptions(int compressionLevel, boolean protection, String comment) {
        this.compressionLevel = compressionLevel;
        this.protection = protection;
        this.comment = comment;
    }
}
