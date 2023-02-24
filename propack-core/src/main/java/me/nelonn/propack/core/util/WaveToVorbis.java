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

package me.nelonn.propack.core.util;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.*;

import java.io.InputStream;
import java.io.OutputStream;

public final class WaveToVorbis {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private static final int READ = 1024;
    private static final byte[] BUFFER = new byte[READ * 4 + 44];

    public static void encode(@NotNull InputStream in, @NotNull OutputStream out) {
        // struct that stores all the static vorbis bitstream settings
        vorbis_info vi = new vorbis_info();

        vorbisenc encoder = new vorbisenc();

        if (!encoder.vorbis_encode_init_vbr(vi, 2, 44100, .3f)) {
            LOGGER.error("Failed to Initialize vorbisenc");
            return;
        }

        // struct that stores all the user comments
        vorbis_comment vc = new vorbis_comment();
        vc.vorbis_comment_add_tag("ENCODER", "Java Vorbis Encoder");

        // central working state for the packet->PCM decoder
        vorbis_dsp_state vd = new vorbis_dsp_state();

        if (!vd.vorbis_analysis_init(vi)) {
            LOGGER.error("Failed to Initialize vorbis_dsp_state");
            return;
        }

        // local working space for packet->PCM decode
        vorbis_block vb = new vorbis_block(vd);

        java.util.Random generator = new java.util.Random();  // need to randomize seed
        // take physical pages, weld into a logical stream of packets
        ogg_stream_state os = new ogg_stream_state(generator.nextInt(256));

        LOGGER.debug("Writing header");
        ogg_packet header = new ogg_packet();
        ogg_packet header_comm = new ogg_packet();
        ogg_packet header_code = new ogg_packet();

        vd.vorbis_analysis_headerout(vc, header, header_comm, header_code);

        os.ogg_stream_packetin(header); // automatically placed in its own page
        os.ogg_stream_packetin(header_comm);
        os.ogg_stream_packetin(header_code);

        // one Ogg bitstream page.  Vorbis packets are inside
        ogg_page og = new ogg_page();
        // one raw packet of data for decode
        ogg_packet op = new ogg_packet();

        try {
            while (os.ogg_stream_flush(og)) {
                out.write(og.header, 0, og.header_len);
                out.write(og.body, 0, og.body_len);
            }
            LOGGER.debug("Done");

            LOGGER.debug("Encoding");
            boolean eos = false;
            while (!eos) {

                int i;
                int bytes = in.read(BUFFER, 0, READ * 4); // stereo hardwired here

                if (bytes == 0) {
                    // end of file.  this can be done implicitly in the mainline,
                    // but it's easier to see here in non-clever fashion.
                    // Tell the library we're at end of stream so that it can handle
                    // the last frame and mark end of stream in the output properly
                    vd.vorbis_analysis_wrote(0);
                } else {
                    // data to encode

                    // expose the buffer to submit data
                    float[][] buffer = vd.vorbis_analysis_buffer(READ);

                    // uninterleave samples
                    for (i = 0; i < bytes / 4; i++) {
                        buffer[0][vd.pcm_current + i] = ((BUFFER[i * 4 + 1] << 8) | (0x00ff & BUFFER[i * 4])) / 32768.f;
                        buffer[1][vd.pcm_current + i] = ((BUFFER[i * 4 + 3] << 8) | (0x00ff & BUFFER[i * 4 + 2])) / 32768.f;
                    }

                    // tell the library how much we actually submitted
                    vd.vorbis_analysis_wrote(i);
                }

                // vorbis does some data preanalysis, then divvies up blocks for more involved
                // (potentially parallel) processing.  Get a single block for encoding now

                while (vb.vorbis_analysis_blockout(vd)) {

                    // analysis, assume we want to use bitrate management

                    vb.vorbis_analysis(null);
                    vb.vorbis_bitrate_addblock();

                    while (vd.vorbis_bitrate_flushpacket(op)) {

                        // weld the packet into the bitstream
                        os.ogg_stream_packetin(op);

                        // write out pages (if any)
                        while (!eos) {
                            if (!os.ogg_stream_pageout(og)) {
                                break;
                            }

                            out.write(og.header, 0, og.header_len);
                            out.write(og.body, 0, og.body_len);

                            // this could be set above, but for illustrative purposes, I do
                            // it here (to show that vorbis does know where the stream ends)
                            if (og.ogg_page_eos() > 0) {
                                eos = true;
                            }
                        }
                    }
                }
            }
            LOGGER.debug("Done");
        } catch (Exception e) {
            throw new RuntimeException("Unable to convert wav to ogg", e);
        }
    }

    private WaveToVorbis() {
        throw new UnsupportedOperationException();
    }
}