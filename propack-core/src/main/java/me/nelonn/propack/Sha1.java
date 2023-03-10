/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Nelonn <two.nelonn@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nelonn.propack;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha1 {
    private final byte[] bytes;
    private final String string;

    private Sha1(byte @NotNull [] bytes, @NotNull String string) {
        this.bytes = bytes;
        this.string = string;
    }

    public byte @NotNull [] asBytes() {
        byte[] res = new byte[bytes.length];
        System.arraycopy(bytes, 0, res, 0, bytes.length);
        return res;
    }

    public @NotNull String asString() {
        return string;
    }

    @Override
    public String toString() {
        return asString();
    }

    public static @NotNull Sha1 hash(@NotNull InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = bytes(inputStream);
        String string = bytesToString(bytes);
        return new Sha1(bytes, string);
    }

    public static @NotNull Sha1 fromHashBytes(byte @NotNull [] hash) {
        byte[] bytes = new byte[hash.length];
        System.arraycopy(hash, 0, bytes, 0, hash.length);
        String string = bytesToString(bytes);
        return new Sha1(bytes, string);
    }

    private static byte @NotNull [] bytes(@NotNull InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[8 * 1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return digest.digest();
    }

    private static @NotNull String bytesToString(byte @NotNull [] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            int value = b & 0xFF;
            if (value < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(value));
        }
        return sb.toString();
    }
}
