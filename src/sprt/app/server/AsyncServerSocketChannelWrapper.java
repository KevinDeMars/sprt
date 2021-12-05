/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import sprt.serialization.NIODeframer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static shared.app.AppUtil.logToFile;

/**
 * Wrapper that makes AsynchronousServerSocketChannel less obnoxious to use
 */
public class AsyncServerSocketChannelWrapper implements AutoCloseable {
    private static final Logger LOG = logToFile(AsyncServerSocketChannelWrapper.class, "connections.log");
    private final static int BUF_SIZE = 1024;
    private final static int TIMEOUT_SECONDS = 20;
    // Underlying channel to use
    private final AsynchronousServerSocketChannel ch;

    /**
     * Wraps an AsynchronousSocketChannel obtained by having a peer connect
     */
    static class Peer implements AutoCloseable {
        // Underlying channel
        public final AsynchronousSocketChannel ch;
        // used for channel read/write methods
        public final ByteBuffer buf;
        // Used to deframe incoming messages from the peer
        private final NIODeframer deframer;

        /**
         * Creates a new Peer that wraps the given channel.
         * @param peer channel to wrap
         */
        public Peer(AsynchronousSocketChannel peer) {
            this.ch = peer;
            buf = ByteBuffer.allocateDirect(BUF_SIZE);
            deframer = new NIODeframer();
        }

        @Override
        public void close() {
            try {
                ch.close();
            }
            catch (IOException e) {
                LOG.log(Level.WARNING, "Can't close channel", e);
            }
        }

        /**
         * Begins reading a message. When finished (according to the deframer),
         *   the callback will be run with the data read (including delimiter).
         *
         * If the read fails, the channel will be closed.
         * @param callback Method to run when message is obtained
         */
        public void beginRead(Consumer<byte[]> callback) {
            buf.clear();
            doBeginRead(callback);
        }

        private void doBeginRead(Consumer<byte[]> callback) {
            ch.read(buf, TIMEOUT_SECONDS, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer numRead, Void attachment) {
                    if (numRead == -1) {
                        close();
                        return;
                    }
                    // Add the portion of the buffer with the new data to the deframer
                    var newData = buf.slice(buf.position() - numRead, numRead);
                    var msg = deframer.getMessage(newData);
                    if (msg == null) {
                        // not full message: start another read
                        doBeginRead(callback);
                    }
                    else {
                        // full message: run user callback w/ msg
                        callback.accept(msg);
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    LOG.log(Level.INFO, "Read failed", exc);
                    close();
                }
            });
        }

        /**
         * Writes the given data to the peer. When finished, runs the given callback.
         *
         * If the write fails, the channel is closed.
         * @param data data to write to peer
         * @param onCompletion callback to run when finished
         */
        public void beginWrite(byte[] data, VoidFunction onCompletion) {
            buf.clear();
            buf.put(data);
            buf.flip();
            doBeginWrite(onCompletion);
        }

        private void doBeginWrite(VoidFunction onCompletion) {
            ch.write(buf, TIMEOUT_SECONDS, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    if (buf.hasRemaining()) {
                        doBeginWrite(onCompletion);
                    }
                    else {
                        buf.clear();
                        onCompletion.run();
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    LOG.log(Level.INFO, "Write failed", exc);
                    close();
                }
            });
        }

        /**
         * Gets the address of the peer/remote side.
         * @return Peer address
         * @throws IOException if address can't be obtained
         */
        public SocketAddress getRemoteAddress() throws IOException {
            return ch.getRemoteAddress();
        }
    }

    /**
     * Creates a new channel and binds to the given port.
     * @param port port to bind to
     * @throws IOException If channel creation or binding fails
     */
    public AsyncServerSocketChannelWrapper(int port) throws IOException {
        ch = AsynchronousServerSocketChannel.open();
        ch.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        ch.bind(new InetSocketAddress(port));
    }

    @Override
    public void close() {
        try {
            ch.close();
        }
        catch (IOException e) {
            LOG.warning("Couldn't close server channel");
        }
    }

    /**
     * Begins accepting a new peer. When a peer connects, the callback is run.
     * The return value of the callback indicates whether to continue accepting more peers.
     *
     * @param onNewConnection Callback to run when peer connects
     */
    public void beginAccept(Function<Peer, Boolean> onNewConnection) {
        ch.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel newCh, Void attachment) {
                var peer = new Peer(newCh);
                boolean repeat = onNewConnection.apply(peer);
                if (repeat)
                    beginAccept(onNewConnection);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                LOG.log(Level.INFO, "Failed to accept peer", exc);
                close();
            }
        });
    }


}
