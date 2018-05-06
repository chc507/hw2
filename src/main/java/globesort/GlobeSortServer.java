package globesort;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class GlobeSortServer {
    private Server server;

	private static int MAX_MESSAGE_SIZE = 100 * 1024 * 1024;

    private void start(String ip, int port) throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(ip, port))
                    .addService(new GlobeSortImpl())
					.maxMessageSize(MAX_MESSAGE_SIZE)
                    .executor(Executors.newFixedThreadPool(10))
                    .build()
                    .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                GlobeSortServer.this.stop();
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static Namespace parseArgs(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("GlobeSortClient").build()
                .description("GlobeSort client");
        parser.addArgument("server_port").type(Integer.class)
                .help("Server port");
        parser.addArgument("-a", "--address").type(String.class).setDefault("0.0.0.0")
                .help("Server IP address to bind to");

        Namespace res = null;
        try {
            res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            parser.printUsage();
            System.exit(1);
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        Namespace cmd_args = parseArgs(args);
        if (cmd_args == null) {
            throw new RuntimeException("Argument parsing failed");
        }

        final GlobeSortServer server = new GlobeSortServer();
        server.start(cmd_args.getString("address"), cmd_args.getInt("server_port"));
        server.blockUntilShutdown();
    }

    static class GlobeSortImpl extends GlobeSortGrpc.GlobeSortImplBase {
        @Override
        public void ping(Empty req, final StreamObserver<Empty> responseObserver) {
            Empty response = Empty.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void sortIntegers(IntArray req, final StreamObserver<IntArray> responseObserver) {
            //long startTime = System.nanoTime();
            Integer[] values = req.getValuesList().toArray(new Integer[req.getValuesList().size()]);
            long startTime = System.nanoTime();
            Arrays.sort(values);
            Long endTime = System.nanoTime();
            Long elapsedTime = endTime - startTime;
            System.out.println("With Long " + elapsedTime);
            System.out.println("With Int " + elapsedTime.intValue());
            
            IntArray.Builder responseBuilder = IntArray.newBuilder();
            /*
            for(Integer val : values) {
                responseBuilder.addValues(val);
            }
            */
            responseBuilder.addValues((elapsedTime.intValue());
            /*
                https://github.com/chc507/hw2.git
            */

            IntArray response = responseBuilder.build();
            responseObserver.onNext(response);
            //responseObserver.onNext(elapsedTime);            
            responseObserver.onCompleted();
            //long endTime = System.nanoTime();
            //long elapsedTime = endTime - startTime;
            //return elapsedTime;
        }
    }
}
