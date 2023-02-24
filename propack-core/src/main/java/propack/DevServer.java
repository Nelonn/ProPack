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

package propack;

import me.nelonn.propack.UploadedPack;
import me.nelonn.propack.builder.Hosting;
import me.nelonn.propack.core.UploadedPackImpl;
import me.nelonn.propack.core.util.LogManagerCompat;
import me.nelonn.propack.core.util.NamedThreadFactory;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class DevServer implements Hosting {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private HttpRunner runner;
    private String returnUrl;
    private File file;

    public void enable(@NotNull Map<String, Object> options) {
        if (runner != null) return;
        int port;
        Object portRaw = options.getOrDefault("port", 3000);
        if (portRaw instanceof Number) {
            port = ((Number) portRaw).intValue();
        } else {
            throw new IllegalArgumentException("Expected 'port' to be a number");
        }
        Object returnIpRaw = options.getOrDefault("return_ip", "127.0.0.1");
        if (!(returnIpRaw instanceof String)) {
            throw new IllegalArgumentException("Expected 'return_ip' to be a string");
        }
        String returnIp = (String) returnIpRaw;
        returnUrl = "http://" + returnIp + ":" + port;
        try {
            runner = new HttpRunner(port, () -> file);
            new Thread(runner, "devhttp-server").start();
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong when running http server", e);
        }
    }

    public void disable() {
        if (runner != null) {
            try {
                runner.close();
                LOGGER.info("Server is turned off");
            } catch (Exception ignored) {
            }
            runner = null;
        }
        returnUrl = null;
    }

    @Override
    public @Nullable UploadedPack upload(@NotNull File file, byte @NotNull [] hash, @NotNull String hashString) {
        this.file = file;
        return new UploadedPackImpl(returnUrl + '/' + hashString + ".zip", hash, hashString);
    }

    public static class HttpRunner implements Runnable, Closeable {
        private volatile boolean shouldStop = false;
        private final ServerSocket serverSocket;
        private final Supplier<File> fileSupplier;
        private final ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory(r -> "devhttp-client-" + r.hashCode()));

        public HttpRunner(int port, @NotNull Supplier<File> fileSupplier) throws IOException {
            serverSocket = new ServerSocket(port);
            this.fileSupplier = fileSupplier;
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

            String[] requestLine = bufferedReader.readLine().split(" ");
            if (requestLine.length != 3) {
                LOGGER.warn("Unexpected request line: {}", Arrays.toString(requestLine));
                socket.close();
                return;
            }

            //String method = requestLine[0];
            //String path = requestLine[1];
            String httpVersion = requestLine[2];

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1), true);

            File result = fileSupplier.get();
            if (result == null) {
                printWriter.println(httpVersion + " 404 Not Found");
                socket.close();
                return;
            }

            try (InputStream inputStream = new FileInputStream(result)) {
                printWriter.println(httpVersion + " 200 OK");
                printWriter.println("Content-Type: application/zip");
                printWriter.println("Content-Length: " + result.length());
                printWriter.println("Server: ProPackDev");
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
