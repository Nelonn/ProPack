/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Michael Neonov <two.nelonn@gmail.com>
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

package me.nelonn.propack.core;

import me.nelonn.propack.Sha1;
import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.hosting.Hosting;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DevServer extends Hosting implements Closeable {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Map<String, File> files = new HashMap<>();
    private final String returnUrl;
    private final HttpRunner runner;

    public DevServer(@NotNull String returnIp, int port) {
        returnUrl = "http://" + returnIp + ":" + port;
        try {
            runner = new HttpRunner(port);
            new Thread(runner, "devhttp-server").start();
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong when running http server", e);
        }
    }

    @Override
    public void close() throws IOException {
        runner.close();
        LOGGER.info("DevServer is turned off");
    }

    @Override
    public @NotNull UploadedPack upload(@NotNull File file, @NotNull Sha1 sha1, @NotNull String name, @Nullable Map<String, Object> options) {
        files.put(sha1.toString(), file);
        return new UploadedPackImpl(name, returnUrl + '/' + sha1.asString() + ".zip", sha1.asBytes(), sha1.asString());
    }

    public class HttpRunner implements Runnable, Closeable {
        private volatile boolean shouldStop = false;
        private final ServerSocket serverSocket;
        private final ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory(r -> "devhttp-client-" + r.hashCode()));

        public HttpRunner(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Dev HTTP server started successfully on port {}", port);
        }

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    Socket socket = serverSocket.accept();
                    executorService.execute(() -> {
                        try {
                            handle(socket);
                        } catch (Exception e) {
                            LOGGER.error("Failed handling request", e);
                        }
                    });
                } catch (IOException e) {
                    if (!shouldStop) LOGGER.error("Failed handling connection", e);
                }
            }
        }

        private void handle(@NotNull Socket socket) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));

            String superHeader = bufferedReader.readLine();
            if (superHeader == null) return;

            String[] requestLine = superHeader.split(" ");
            if (requestLine.length != 3) {
                LOGGER.warn("Unexpected request line: {}", Arrays.toString(requestLine));
                socket.close();
                return;
            }

            //String method = requestLine[0];
            String path = requestLine[1];
            String httpVersion = requestLine[2];

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1), true);

            String sha1 = path.substring(1);
            if (sha1.endsWith(".zip")) {
                sha1 = sha1.substring(0, sha1.length() - ".zip".length());
            }
            File file = files.get(sha1);
            if (file == null) {
                printWriter.println(httpVersion + " 404 Not Found");
                socket.close();
                return;
            }

            try (InputStream inputStream = new FileInputStream(file)) {
                printWriter.println(httpVersion + " 200 OK");
                printWriter.println("Content-Type: application/zip");
                printWriter.println("Content-Length: " + file.length());
                printWriter.println("Server: ProPackDevServer");
                printWriter.println();
                printWriter.flush();

                byte[] buffer = new byte[8 * 1024];
                for (int read; (read = inputStream.read(buffer)) != -1; ) {
                    outputStream.write(buffer, 0, read);
                    outputStream.flush();
                }
                LOGGER.info("Successfully served to {}", socket.getInetAddress());
            } catch (FileNotFoundException e) {
                printWriter.println(httpVersion + " 404 Not Found");
            }
            socket.close();
        }

        @Override
        public void close() throws IOException {
            shouldStop = true;
            serverSocket.close();
            executorService.shutdown();
        }
    }
}
