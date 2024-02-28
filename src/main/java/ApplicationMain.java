
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JFrame;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import screen.GameScreen;
import screen.LoseScreen;
import screen.Screen;
import screen.StartScreen;

import static java.lang.Thread.sleep;

public class ApplicationMain extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private StartScreen startScreen;
    private GameScreen gameScreen;
    private LoseScreen loseScreen;
    private GameScreen record = null;
    private int state;
    private static final String RECORD_FOLDER = "record";
    private static final String ARCHIVE = "archive";
    private static final String FILE_EXTENSION = ".txt";
    private static int counter = 0;

    public ApplicationMain() {
        super();
        terminal = new AsciiPanel(30, 20, AsciiFont.Gold_plated_32x32);
        add(terminal);
        pack();
        //初始化各类屏幕
        startScreen = new StartScreen();
        gameScreen = new GameScreen(0);
        loseScreen = new LoseScreen();
        createRecordFolder();

        state = 0;
        addKeyListener(this);
        repaint();
    }
    private static void createRecordFolder() {
        try {
            Path folderPath = Paths.get(RECORD_FOLDER);
            if (!Files.exists(folderPath)) {
                Files.createDirectory(folderPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void repaint() {
        terminal.clear();
        if (record != null) {//回放模式
            record.displayOutput(terminal, record.getMode());
            super.repaint();
            return;
        }
        Screen screen = switch (state) {
            case 0 -> startScreen;
            case 1 -> gameScreen;
            default -> loseScreen;
        };
        if (screen instanceof StartScreen)
            screen.displayOutput(terminal, -2);//单人模式
        else
            screen.displayOutput(terminal, 0);
        super.repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (state == 0 && (e.getKeyChar() == 'r' || e.getKeyChar() == 's' || e.getKeyChar() == 'd')) {
            if (e.getKeyChar() == 'r') {//读取回放
                new Thread(() -> {
                    loadRecord();
                    System.out.println("完成回放，退出loadRecord。");
                }).start();

            }
            else if (e.getKeyChar() == 's'){//存档读取
                GameScreen s = loadArchive(ARCHIVE + FILE_EXTENSION);
                if (s != null) {
                    gameScreen = s;
                    state = 1;
                    System.out.println("读取存档成功。");
                }
                else
                    System.out.println("存档为空！");

            }
            else {//删除录像
                deleteFolder(new File(RECORD_FOLDER));
                System.out.println("成功删除录像！可以开始新游戏啦！");
            }
        }
        else {
            int old = state;
            switch (state) {
                case 0 -> state = startScreen.respondToUserInput(e, 0);
                case 1 -> state = gameScreen.respondToUserInput(e, 0);
                default -> state = loseScreen.respondToUserInput(e, 0);
            }
            if (old == 0 && state == 1) {
                System.out.println("create snake in solo game!");
                gameScreen.createSnake(0);
            }
        }
        repaint();
    }


    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                app.repaint();
                recording(app);
            }
        };
        timer.scheduleAtFixedRate(task, 0, 100);
    }
    private void loadRecord() {
        File folder = new File(RECORD_FOLDER);
        File[] files = folder.listFiles();

        if (files != null) {
            //排序，顺序读取文件
            Arrays.sort(files, (f1, f2) -> {
                String fileName1 = f1.getName();
                String fileName2 = f2.getName();
                int number1 = extractNumber(fileName1);
                int number2 = extractNumber(fileName2);
                return Integer.compare(number1, number2);
            });
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(FILE_EXTENSION)) {
                    try (FileInputStream fileInputStream = new FileInputStream(file);
                         ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                        record =  (GameScreen) objectInputStream.readObject();
                        repaint();
                        sleep(100);
                    } catch (IOException e) {
//                        e.printStackTrace();
                        System.out.println("IOException!");
                    } catch (ClassNotFoundException e) {
                        System.out.println("ClassNotFoundException!");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            record = null;
        }
    }

    private static int extractNumber(String fileName) {
        String nameWithoutExtension = fileName.replace(FILE_EXTENSION, "");
        String numberString = nameWithoutExtension.substring("record".length());
        return Integer.parseInt(numberString);
    }

    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }
    private static void recording(ApplicationMain app) {
        if (app.state == 1) { //开始游戏才录像
//            System.out.println("在录制");
            File folder = new File(RECORD_FOLDER);
            folder.mkdir(); // 使用mkdir()创建文件夹
            try {
                // 创建新文件
                String fileName = "record" + counter + FILE_EXTENSION;
                File file = new File(RECORD_FOLDER, fileName);

                String archive = ARCHIVE + FILE_EXTENSION;
                File f = new File(archive);

                // 写入 screen 对象到文件
                try (FileOutputStream fileOutputStream1 = new FileOutputStream(file);
                     FileOutputStream fileOutputStream2 = new FileOutputStream(f);
                     ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(fileOutputStream1);
                     ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(fileOutputStream2)) {

                    objectOutputStream1.writeObject(app.gameScreen);
                    objectOutputStream2.writeObject(app.gameScreen);
                } catch (ConcurrentModificationException ignored) {

                }

                // 增加计数器
                counter++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private GameScreen loadArchive(String filePath) {
        GameScreen s = null;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            Object obj = objectInputStream.readObject();
            if (obj instanceof GameScreen) {
                s =  (GameScreen) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("存档读取失败。");
        }
        if (s != null) {
            s.recoverFromArchive();
        }
        return s;
    }
}