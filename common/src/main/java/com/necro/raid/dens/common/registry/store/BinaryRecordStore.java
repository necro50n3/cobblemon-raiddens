package com.necro.raid.dens.common.registry.store;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class BinaryRecordStore<K, V> implements AutoCloseable {
    private static final int MAX_READ_ATTEMPTS = 2;

    private final Path file;
    private final Function<V, byte[]> encoder;
    private final BiFunction<K, byte[], V> decoder;
    private final ExecutorService readExecutor;
    private final ExecutorService writeExecutor;
    private final AtomicLong generation = new AtomicLong();

    private volatile State<K> state = State.empty();
    private volatile boolean closed;

    public BinaryRecordStore(@NotNull Path file, @NotNull Function<V, byte[]> encoder, @NotNull BiFunction<K, byte[], V> decoder) {
        this(file, encoder, decoder, 2);
    }

    public BinaryRecordStore(@NotNull Path file, @NotNull Function<V, byte[]> encoder, @NotNull BiFunction<K, byte[], V> decoder, int readWorkers) {
        if (readWorkers <= 0) throw new IllegalArgumentException("readWorkers must be > 0");

        this.file = file.normalize();
        this.encoder = encoder;
        this.decoder = decoder;

        String name = String.valueOf(file.getFileName());
        this.readExecutor = Executors.newFixedThreadPool(
            readWorkers,
            threadFactory("BinaryRecordStore-Read-" + name)
        );
        this.writeExecutor = Executors.newSingleThreadExecutor(
            threadFactory("BinaryRecordStore-Write-" + name)
        );
    }

    public CompletableFuture<BuildResult<K>> rebuildAsync(@NotNull Iterable<? extends V> values, @NotNull Function<? super V, ? extends K> idFunction) {
        if (this.closed) return closedFuture();
        return submit(() -> rebuild(values, idFunction), this.writeExecutor);
    }

    public boolean contains(K id) {
        return this.state.index().containsKey(id);
    }

    public CompletableFuture<Optional<byte[]>> readBytesAsync(K id) {
        if (this.closed) return closedFuture();

        if (!this.state.index().containsKey(id)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return submit(() -> readBytes(id), this.readExecutor);
    }

    public CompletableFuture<Optional<V>> readAsync(@NotNull K id) {
        if (this.closed) return closedFuture();

        if (!this.state.index().containsKey(id)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return submit(() -> readBytes(id).map(bytes -> decoder.apply(id, bytes)), readExecutor);
    }

    public CompletableFuture<Optional<V>> readAsync(@NotNull K id, @NotNull Executor decodeExecutor) {
        return readBytesAsync(id).thenApplyAsync(
            bytes -> bytes.map(raw -> decoder.apply(id, raw)),
            decodeExecutor
        );
    }

    @Override
    public void close() {
        State<K> previous;
        synchronized (this) {
            if (this.closed) return;
            this.closed = true;
            previous = this.state;
            this.state = State.empty();
        }

        this.writeExecutor.shutdownNow();
        this.readExecutor.shutdownNow();

        closeQuietly(previous.channel());
    }

    public boolean isClosed() {
        return this.closed;
    }

    private BuildResult<K> rebuild(Iterable<? extends V> values, Function<? super V, ? extends K> idFunction) {
        if (this.closed) throw new StorageException("Store has been closed");

        long gen = this.generation.incrementAndGet();
        Path target = this.file.resolveSibling(this.file.getFileName() + "." + gen);
        Path temporary = this.file.resolveSibling(this.file.getFileName() + "." + gen + ".tmp");
        Map<K, Entry> newIndex = new HashMap<>();
        FileChannel newChannel = null;

        try {
            Path parent = this.file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            deleteStaleFiles(this.state.path());

            try (FileChannel out = FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                long offset = 0;

                for (V value : values) {
                    K id = idFunction.apply(value);

                    if (id == null) throw new IllegalArgumentException("Record has a null id");
                    if (newIndex.containsKey(id)) throw new IllegalArgumentException("Duplicate record id: " + id);

                    byte[] bytes = this.encoder.apply(value);
                    writeFully(out, ByteBuffer.wrap(bytes));
                    newIndex.put(id, new Entry(offset, bytes.length));
                    offset += bytes.length;
                }

                out.force(true);
            }

            moveIntoPlace(temporary, target);

            newChannel = FileChannel.open(target, StandardOpenOption.READ);
            long fileSize = newChannel.size();
            Map<K, Entry> published = Collections.unmodifiableMap(newIndex);

            State<K> previous;
            synchronized (this) {
                if (this.closed) throw new IllegalStateException("Store has been closed");
                previous = this.state;
                this.state = new State<>(published, newChannel, target);
            }
            newChannel = null;

            closeQuietly(previous.channel());
            deleteQuietly(previous.path());

            return new BuildResult<>(published, fileSize, published.size());
        } catch (IOException | RuntimeException e) {
            closeQuietly(newChannel);
            deleteQuietly(temporary);
            deleteQuietly(target);
            throw new StorageException("Failed to rebuild " + this.file, e);
        }
    }

    private Optional<byte[]> readBytes(K id) {
        IOException lastFailure = null;

        for (int attempt = 0; attempt < MAX_READ_ATTEMPTS; attempt++) {
            State<K> current = this.state;
            Entry entry = current.index().get(id);

            if (entry == null) return Optional.empty();

            try {
                return Optional.of(readFully(current.channel(), entry.offset(), entry.length()));
            }
            catch (ClosedChannelException e) {
                lastFailure = e;
            }
            catch (IOException e) {
                throw new StorageException("Failed to read record " + id, e);
            }
        }

        throw new StorageException("Failed to read record " + id, lastFailure);
    }

    private static byte[] readFully(FileChannel channel, long offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        long position = offset;

        while (buffer.hasRemaining()) {
            int read = channel.read(buffer, position);
            if (read < 0) throw new IOException("Unexpected end of file at offset " + position);
            position += read;
        }

        return buffer.array();
    }

    private static void writeFully(FileChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static void moveIntoPlace(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void closeQuietly(FileChannel channel) {
        if (channel == null) return;
        try {
            channel.close();
        }
        catch (IOException ignored) {}
    }

    private static void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        }
        catch (IOException ignored) {}
    }

    private void deleteStaleFiles(Path keep) {
        Path parent = this.file.toAbsolutePath().getParent();
        if (parent == null || !Files.isDirectory(parent)) return;

        Path keepAbsolute = keep == null ? null : keep.toAbsolutePath();
        Pattern pattern = Pattern.compile(Pattern.quote(String.valueOf(this.file.getFileName())) + "\\.\\d+(\\.tmp)?");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
            for (Path candidate : stream) {
                if (!pattern.matcher(String.valueOf(candidate.getFileName())).matches()) continue;
                if (candidate.toAbsolutePath().equals(keepAbsolute)) continue;
                deleteQuietly(candidate);
            }
        }
        catch (IOException ignored) {}
    }

    private static <T> CompletableFuture<T> submit(Supplier<T> task, Executor executor) {
        try {
            return CompletableFuture.supplyAsync(task, executor);
        }
        catch (RejectedExecutionException e) {
            return CompletableFuture.failedFuture(new StorageException("Store is closed", e));
        }
    }

    private static ThreadFactory threadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static <T> CompletableFuture<T> closedFuture() {
        return CompletableFuture.failedFuture(new StorageException("Store has been closed"));
    }

    public record Entry(long offset, int length) {
        public Entry {
            if (offset < 0) throw new IllegalArgumentException("offset cannot be negative");
            if (length < 0) throw new IllegalArgumentException("length cannot be negative");
        }
    }

    public record BuildResult<K>(Map<K, Entry> index, long fileSize, int recordCount) {}

    public static final class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record State<K>(Map<K, Entry> index, FileChannel channel, Path path) {
        static <K> State<K> empty() {
            return new State<>(Map.of(), null, null);
        }
    }
}