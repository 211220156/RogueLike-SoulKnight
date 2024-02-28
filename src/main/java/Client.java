import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import screen.Screen;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Thread.sleep;

/**
 *
 * Test client for NIO server
 *
 */
public class Client extends JFrame implements KeyListener {
    private AsciiPanel terminal;
    private Screen screen;
    private int id = -1;
    private int keyCode;
    private Selector selector;
    private SocketChannel clientChannel;
    public Client() {
        super();
        terminal = new AsciiPanel(30, 20, AsciiFont.Gold_plated_32x32);
        add(terminal);
        pack();
        addKeyListener(this);
    }
    @Override
    public void repaint() {
        terminal.clear();
        screen.displayOutput(terminal, id);
        super.repaint();
    }

    public void startClient() throws IOException, InterruptedException {
        try {
            clientChannel = SocketChannel.open(new InetSocketAddress("localhost", 9093));
            clientChannel.configureBlocking(false);
            selector = Selector.open();
            clientChannel.register(selector, SelectionKey.OP_READ);
        } catch (ConnectException e) {
            System.out.println("Connection refused. Please make sure the server is turned on.");
            return;
        }

        while (true) {
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                // Remove key from set so we don't process it twice
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isReadable()) { // Read from server
                    this.read(key);
                }
            }
        }

    }
    private void read(SelectionKey key) throws IOException {
        System.out.println("in client read!");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(80000);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            System.out.println("cannot read from server!");
            channel.close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println("get data from server! data num :" + data.length);

        if (this.id == -1) {
            this.id = deserializeID(data);
            System.out.println("client get id " + id);
        }
        else {
            screen = deserializeScreenObject(data);
            if (screen != null) repaint();
        }
    }
    private void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
        if (clientChannel == null) {
            System.out.println("还没建立连接。");
            return;
        }
        if (clientChannel.isConnected()) writeDataToWritableChannels();
    }
    public static void main(String[] args) {
        Runnable clientTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Client client = new Client();
                    client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    client.setVisible(true);
                    client.startClient();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(clientTask).start();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        setKeyCode(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    public static Screen deserializeScreenObject(byte[] data) {
//        System.out.println("客户端收到的data数：" + data.length);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            try {
                Object obj = ois.readObject();
                if (obj instanceof Screen) {
                    return (Screen) obj;
                } else {
                    throw new IllegalArgumentException("Invalid object type. Expected Screen.");
                }
            } catch (EOFException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static int deserializeID(byte[] data) {
//        System.out.println("客户端收到的data数：" + data.length);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            try {
                int id = (int)ois.readObject();
                return id;
            } catch (EOFException e) {
                e.printStackTrace();
                return -1;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
    private void writeDataToWritableChannels() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(this.keyCode);
        System.out.println("client keycode : " + keyCode);
        System.out.println("put int!");
        buffer.flip();
        try {
            while (buffer.hasRemaining()) {
                System.out.println("客户写入键盘码给server：" + clientChannel.write(buffer));
            }
        } catch (IOException e) {
            // 处理写入异常
        }
    }
}