import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import screen.LoseScreen;
import screen.Screen;
import screen.GameScreen;
import screen.StartScreen;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * This is a simple NIO based server.
 *
 */
public class EchoNIOServer extends JFrame {
    private final AsciiPanel terminal;
    private GameScreen gameScreen;
    private StartScreen startScreen;
    private LoseScreen loseScreen;
    private Selector selector;
    private ScheduledExecutorService scheduledExecutor;
    private static int clientID = 0;
    private final InetSocketAddress listenAddress;
    private final static int PORT = 9093;
    private final List<SelectionKey> clients;
    // states. 0 : startScreen, 1 : gameScreen, 2 : loseScreen
    private final HashMap<Integer, Integer> states;
    private static final String RECORD_FOLDER = "record";
    private static final String FILE_EXTENSION = ".txt";
    private static int counter = 0;


    public static void main(String[] args)  {
        try {
            EchoNIOServer server = new EchoNIOServer("localhost", 9093);
            server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            server.setVisible(true);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EchoNIOServer(String address, int port)  {
        super();
        terminal = new AsciiPanel(30, 20, AsciiFont.Gold_plated_32x32);
        add(terminal);
        pack();
        //初始化屏幕
        startScreen = new StartScreen();
        loseScreen = new LoseScreen();
        gameScreen = new GameScreen(-1);

        states = new HashMap<>();

        repaint();
        listenAddress = new InetSocketAddress(address, PORT);
        clients = new ArrayList<>();
    }
    @Override
    public void repaint() {
        terminal.clear();
        gameScreen.displayOutput(terminal, -1);
        super.repaint();
    }

    /**
     * Start the server
     *
     * @throws IOException
     */
    private void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port >> " + PORT);

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                repaint();
                recording();
                writeDataToWritableChannels();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);


        while (true) {
            // wait for events
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }

            // process selected keys...
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                // Remove key from set so we don't process it twice
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) { // Accept client connections
                    this.accept(key);
                } else if (key.isReadable()) { // Read from client
                    this.read(key);
                } else if (key.isWritable()) {
                    // write data to client...

                }
            }
        }
    }

    // accept client connection
    private void accept(SelectionKey key) throws IOException {
        System.out.println("accept start!");
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        /*
         * Register channel with selector for further IO (record it for read/write
         * operations, here we have used read operation)
         */
        SelectionKey clientKey = channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        clientKey.attach(clientID);

        clients.add(clientKey);
        channel.write(ByteBuffer.wrap(serializeObject(clientID)));


        states.put(clientID, 0);

        writeDataToWritableChannels();
        clientID++;
        System.out.println("accept finish!");

    }

    // read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            channel.read(buffer);
        } catch (SocketException e) {
            //客户退出，将key标记为false，不再发送信息。
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            // 在这remove key。
            gameScreen.removeSnake((int)key.attachment());
            return;
        }

        buffer.flip();
        int keyCode = buffer.getInt();
        int client = (int) key.attachment();
        update(keyCode, client);
    }
    private void writeDataToWritableChannels() {
        for (SelectionKey key : clients) {
            if (key.isValid()) {
                int client = (int) key.attachment();
                Screen screen = switch (states.get(client)) {
                    case 0 -> startScreen;
                    case 1 -> gameScreen;
                    default -> loseScreen;
                };
                //序列化screen
                byte[] serializedObject = serializeObject(screen);
                ByteBuffer buffer = ByteBuffer.allocate(serializedObject.length);
                buffer.clear();
                if (serializedObject != null) {
                    buffer.put(serializedObject);
                }
                buffer.flip();
                try {
                    while (buffer.hasRemaining()) {
                        ((SocketChannel) key.channel()).write(buffer);
                    }
                } catch (IOException e) {
                    // 处理写入异常
                }
            }
        }
    }
    public static byte[] serializeObject(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            try {
                oos.writeObject(object);
            } catch (ConcurrentModificationException e) {
                //并发bug，catch了就等于没bug ^_^
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void update(int keyCode, int client) {
        KeyEvent e = new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, keyCode, KeyEvent.CHAR_UNDEFINED);
        int old = states.get(client);
        switch (states.get(client)) {
            case 0 -> states.put(client, startScreen.respondToUserInput(e, client));
            case 1 -> states.put(client, gameScreen.respondToUserInput(e, client));
            default -> states.put(client, loseScreen.respondToUserInput(e, client));
        }
        if (old == 0 && states.get(client) == 1) {
            System.out.println("create snake!!!!!!!!!!!!!!!!!!!!!");
            gameScreen.createSnake(client);
        }
        writeDataToWritableChannels();
        repaint();
    }
    private void recording() {
        //录制
//        System.out.println("在录制");
        File folder = new File(RECORD_FOLDER);
        folder.mkdir(); // 使用mkdir()创建文件夹
        try {
            // 创建新文件
            String fileName = "record" + counter + FILE_EXTENSION;
            File file = new File(RECORD_FOLDER, fileName);

            // 写入 screen 对象到文件
            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

                objectOutputStream.writeObject(gameScreen);
            } catch (ConcurrentModificationException ignored) {

            }

            // 增加计数器
            counter++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}