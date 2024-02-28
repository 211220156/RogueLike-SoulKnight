# J05 并发部分



学号：211220156

姓名：高羲俊

在j05中用多线程实现了Roguelike风格的对战游戏的基本框架。

### 功能介绍



#### 1. 多线程实现游戏生物的自主行为



在本阶段中，用多线程实现了游戏中随机生成怪兽`Monster`。每一个怪兽都由一个线程来控制它的行为。怪兽每隔一段时间就会选择移动或攻击两种行为（用 `TimerTask` 实现），在主世界`world`中用一个线程池来管理怪物线程，当怪兽死亡后，线程被回收复用，提高资源利用率。（详细代码查看`MonsterAI.java`）

怪兽可选择远程攻击行为，即向周围八个方向发射子弹。每个子弹`Bullet`有各类属性(位置，伤害，颜色，方向等)，也实现了`Runnable`接口，即每个子弹也是一个线程。子弹也归为`world`管，当子弹线程发现碰到生物则发动攻击行为，同时子弹消失；碰到墙体直接消失；其他时候沿着方向一直前进。 (详细代码查看`Bullet.java`)

#### 2.避免并发问题的一些举措



- 为了避免两个生物进入一个方块，我定义了和方块`tiles[][]`数组大小一致的Object数组`Object[][] lockTile`，用于对方块进行互斥访问。同时有数组`Long[][] ownership`规定了每个方块当前属于谁。即某个生物(线程)一旦占有了某方块，那么它会在该方块对应位置的`ownership[i][j]`上写入该线程的线程ID，配合`Object[][] lockTile`使用，对某个方块上锁后才允许修改该方块的`ownership`，避免了争用。

  生物体移动的时候也是，先取得下一个位置的锁，占用了新方块，释放旧方块的所有权，更新生物位置，释放新位置的锁，完成移动。

- 由于生物死亡会修改世界`world`中`List<Creature> creatures`的信息，若同时有另一个线程在遍历所有生物则会引发`ConcurrentModificationException`。因此遍历和修改生物时都应该上锁。子弹同理。在实现上两类各采用了一个可重入锁`Lock creatureLock = new ReentrantLock();`,遍历和修改前都应先取得锁，避免引发异常。

### 成果录屏



[j05 并发部分_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1nc411Q7rv/?vd_source=4729de3939b68fbfb11b838e515c1fe1)





# J08 IO功能



学号：211220156

姓名：高羲俊

## 地图保存/加载功能



与`RogueLike Game`每次开始都随机生成地图不同，本游戏的地图保存在目录`src/main/java/resources`下的`map.txt`中，里面存放了二维正整数矩阵，每个位置不同数字表示不同的地形。除了地图边界外，有以下几种地形：

**空地`FLOOR`**：玩家、怪兽、豆子都可以在上面，子弹可以在空地上飞行。

**墙体`WALL`**：障碍物，生物不能进入墙体，子弹碰到会消失。

**草地`GRASS`**：玩家可以进入，怪兽不会进入，玩家进入后将隐藏自己，怪兽无法看到玩家。草地会被怪兽和玩家的子弹破坏，被破坏后变成空地。

**河流`WATER`**：玩家可以潜水，怪兽不会，子弹不会破坏水流，水流可以吞噬所有子弹。

从文件中读取地图的代码如下：

```
String fileName = "/map.txt";

try (InputStream inputStream = WorldBuilder.class.getResourceAsStream(fileName);
	BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
	String line;
   	while ((line = reader.readLine()) != null) {
		// 读取操作
    }
} catch (IOException e) {
	e.printStackTrace();
}
```



`WorldBuilder.class.getResourceAsStream(fileName)` 是使用 `WorldBuilder` 类的类加载器（ClassLoader）来获取指定路径下文件的输入流。`getResourceAsStream()` 方法返回一个 `InputStream` 对象用于创建`BufferedReader`对象。

相较于普通的`InputStream inputStream = new InputStream(filePath)`，因为我们的地图`map.txt`所处位置特殊（处于maven结构resources目录下），所以需要通过类加载器来获取相对路径，而不是绝对路径。

## 进度保存/进度恢复



为了实现进度的保存，我们需要实时的记录游戏画面的所有信息，即一个`GameScene`对象的所有信息。该目的可以通过**序列化/反序列化 + 文件IO**来实现。

首先我们所有场景的父接口`Screen`必须实现接口`Serializable`以便于将对象信息写入文件/从文件恢复。此外，游戏场景`GameScreen`的所有成员对象必须也都要实现`Serializable`接口。

有几个踩坑的点：

1. 线程池`ScheduledExecutorService`和`ExecutorService`是不可以被序列化的，因此在序列化时需要将其忽略。为了实现这一点，我们可以为线程池的声明加上关键字`transient`，告诉编译器序列化时将其忽略。而从文件中读取`GameScreen`对象数据恢复时，需要重新开一个线程池。

2. 在j05时为了实现多线程环境下怪兽Monster和玩家对地图方块`Tile[][]`的互斥访问，我使用了`Object[][] lockTile`来实现每个方块的互斥访问：

   ```
   synchronized (lockTile[i][j]) {
   	//方块访问
   }
   ```

   

   而`Object`也是无法被序列化的。因此也要用`transient`修饰，读取数据恢复时重新创建。

序列化是通过`ByteArrayOutputStream`和`ObjectOutputStream`两个类实现，将一个实现了`Serializable`接口的对象转化为字节信息，再将字节信息写入文件：

```
public static byte[] serializeObject(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
```



反序列化则通过`ByteArrayInputStream`和`ObjectInputStream`两个类实现：

```
public static Screen deserializeScreenObject(byte[] data) {
	try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
    	ObjectInputStream ois = new ObjectInputStream(bis)) {
        try {
            Object obj = ois.readObject();
            if (obj instanceof Screen) {//将读取的对象转化为Screen类对象，反序列化。
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
```



**每次游戏界面刷新，都会进行写文件操作，将当前`GameScreen`对象的信息序列化写入到存档文件`Archive.txt`中，同时覆盖掉旧的信息。因此进度恢复时从`Archive.txt`中读取，反序列化即可。**

具体效果看录屏展示。**本游戏时只保存最近一次存档，在游戏界面中可选择加载存档（在开始界面按s）或者直接开始新游戏。存档为空/加载存档成功都会有提示。**

进度保存/恢复功能只对单人模式（ApplicationMain）下有用。多人模式（EchoNIOServer/Client）下无法恢复进度：其他玩家已经退出连接，此时有一个玩家选择恢复进度的话是不现实的。

## 游戏过程录制/回放



游戏过程录制其实就是从开始之后，游戏过程中不断的将`GameScreen`对象的值写入文件进行保存，回放时按照顺序从文件读取重现即可。也是用**序列化/反序列化 + 文件IO**来实现。

为了实现这一点，程序在每次屏幕刷新，即调用`repaint()`函数时进行写文件操作，将当前游戏场景对象的信息写入文件。每一帧的信息都单独保存在一个独立的`recordXX.txt`中，其中`XX`按照写顺序递增，所有`txt`保存在文件夹`record`内。

下面是录制的函数：

```
private static void recording(ApplicationMain app) {
	if (app.state == 1) { //开始游戏才录像
        System.out.println("在录制");
        File folder = new File(RECORD_FOLDER);
        folder.mkdir(); // 使用mkdir()创建文件夹
        try {
            // 创建新文件
            String fileName = "record" + counter + FILE_EXTENSION;
            File file = new File(RECORD_FOLDER, fileName);//写录像
            String archive = ARCHIVE + FILE_EXTENSION;
            File f = new File(archive);//写存档
            // 写入 screen 对象到文件，利用序列化
            try (FileOutputStream fileOutputStream1 = new FileOutputStream(file);
                 FileOutputStream fileOutputStream2 = new FileOutputStream(f);
                 ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(fileOutputStream1);
                 ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(fileOutputStream2)) {

                objectOutputStream1.writeObject(app.gameScreen);
                objectOutputStream2.writeObject(app.gameScreen);
            } catch (ConcurrentModificationException ignored) {
            
            }

            // 增加计数器，表示写到第几个文件
            counter++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```



而加载录像的函数则从record文件夹中按顺序读取txt文件中的screen对象，按照相同的屏幕刷新率进行回放（注意该函数**需要创建一个新线程执行，因为频繁的调用repaint()函数会与游戏主线程冲突**）：

```
private void loadRecord() {
	File folder = new File(RECORD_FOLDER);
    File[] files = folder.listFiles();//获取文件

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
            if (file.isFile() && file.getName().endsWith(FILE_EXTENSION)) {//确保是回放文件
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    record =  (GameScreen) objectInputStream.readObject();//读取游戏场景对象
                    repaint();//重新绘制场景
                    sleep(100);//刷新率控制
                } catch (IOException e) {
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
```



具体效果看录屏展示。**单人模式和多人模式都可以录屏、回放。每次游戏自动录制，保存到record文件夹下。在开始界面按r开始观看回放，按d可以删除回放信息，以方便新游戏的录制。若回放不存在/回放播放结束/删除回放成功，都会在控制台给出提示。**

## 录屏展示



[java高级程序设计大作业IO部分：地图保存/加载、游戏进度保存/恢复、游戏过程录制/回放_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1iw41147Dk/?vd_source=4729de3939b68fbfb11b838e515c1fe1)







# J09 网络通信功能

学号：211220156

姓名：高羲俊



## 支持多方（大于两方）对战

为了达到server和多个client进行通信，我的设计理念是：**单独出一个server维护所有`Screen`类对象（初始化时新建好），并且维护连接到server的所有玩家，每个client对应一个玩家，有自己的id（server根据玩家数量分配给client），同时维护所有client的所处状态（client的状态转移类似于自动机）。**

```java
private GameScreen gameScreen;//统一的游戏场景
private StartScreen startScreen;//开始界面
private LoseScreen loseScreen;//结束界面
private static int clientID = 0;//每个用户特有的id，从0开始递增
private final List<SelectionKey> clients;//所有用户对应的SelectionKey
// states. 0 : startScreen, 1 : gameScreen, 2 : loseScreen
private final HashMap<Integer, Integer> states;//维护用户状态
```

**server会根据每个client目前所处状态向其发送不同的游戏场景**.比如0表示当前client处于开始界面，server向其发送`startScreen`；1表示当前client处于游戏中，向其发送`gameScreen`；2表示client已经失败，向其发送`loseScreen`.根据服务器刷新率，**server每隔100ms会向所有已连接的client发送对应的内容**。

而**client不需要对游戏场景做出任何改动，client要做的仅仅是接收`Screen`对象，并使用`AsciiPanel`把`Screen`对象在窗口中绘制出来。同时client需要捕捉用户的键盘事件**，将对应的键盘事件发送给server，server将替client将它的键盘输入对`gameScreen`做出相应更新。

上述代码中`List<SelectiohnKey>`的作用以及连接建立、数据传输过程的细节下面阐述。





#### 使用NIO Selector实现

如下图所示：

![img](https://miro.medium.com/v2/resize:fit:816/1*SFoZ6O-3bsvf_8zD9kOAHg.png)

若每个client到来，server都新建一个线程与对应的client进行交互，若有1000个client，server必须创建1000个线程，一旦client数量过大，server会直接瘫痪：它将会花费所有时间用于线程间的调度，效率极低。

因此使用NIO Selector来避免这个问题：**server仅仅使用一个线程开启非阻塞模式的`Selector`，每个client来临时，开启一条通道`channel`与`Selector`连接并注册感兴趣的事件(read/write)。**server只需要每次循环用`selector.selectedKeys()`找出有消息的`channel`并对其做出相应处理。

而由于server是源源不断通过client与selector的channel向client发送消息的，**所以client也需要在有消息时进行接收，因此我的实现中client也需要一个selector来管理这条通道**，有需要时再调用`read`函数。

##### 

#### client与server建立连接

server初始化向selector注册，说明对连接请求感兴趣：

```java
this.selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false);
// bind server socket channel to port
serverChannel.socket().bind(listenAddress);
serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
```

client通过以下代码向server发送建立连接请求：

```java
clientChannel = SocketChannel.open(new InetSocketAddress("localhost", 9093));
```

表示新建一个连接到服务器监听的9093号端口.当server接收到这个请求后调用accept函数完成连接。**在实现中我利用`SelectionKey`的`attachment`标记每一条`channel`对应的`clientID`.同时完成连接后向客户端发送clientID告知client它自己的编号，并在维护信息中新增当前client的状态为0：即还未开始游戏。**

```java
private void accept(SelectionKey key) throws IOException {
    System.out.println("accept start!");
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    SocketChannel channel = serverChannel.accept();//接受连接，产生一个新channel
    channel.configureBlocking(false);
    Socket socket = channel.socket();
    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
    System.out.println("Connected to: " + remoteAddr);//打印连接信息

    SelectionKey clientKey = channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);//将新产生的这个channel注册到selector上，对读和写事件感兴趣。

    clientKey.attach(clientID);//利用SelectionKey的attachment，将这条channel对应的clientID附加到channel对应的SelectionKey上
    clients.add(clientKey);//server维护的所有client对应的SelectionKey,方便发送信息
    channel.write(ByteBuffer.wrap(serializeObject(clientID)));//发送client的ID给client
    states.put(clientID, 0);//哈希表维护用户的状态，0表示还没开始游戏

    writeDataToWritableChannels();//发送Screen对象给client
    clientID++;//clientID自增
    System.out.println("accept finish!");
}
```

而client收到服务器发回的id后，记录下自己的编号，然后就可以从服务器接收`Screen`对象信息，反序列化后渲染到窗口上。



#### client和server互发信息

client给server发送信息当且仅当捕获到用户键盘事件，通过以下代码段向server发送键盘码：

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
buffer.putInt(this.keyCode);
buffer.flip();
try {
	while (buffer.hasRemaining()) {
		System.out.println("客户写入键盘码给server：" + clientChannel.write(buffer));
    }
} catch (IOException e) {
    // 处理写入异常
}
```

server收到后通过以下代码解析：

```java
ByteBuffer buffer = ByteBuffer.allocate(1024);
buffer.flip();
int keyCode = buffer.getInt();
int client = (int) key.attachment();
update(keyCode, client);
```

然后在update中通过生成一个相应的键盘事件，并根据接收到信息的`clientChannel`对应的`SelectionKey`上的`attachment`来区分时哪个client发送过来的键码，`gameScreen`中找出这个`client`对应的角色，响应这个键盘事件：

```java
private void update(int keyCode, int client) {
	KeyEvent e = new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, keyCode, KeyEvent.CHAR_UNDEFINED);
    int old = states.get(client);//保存该client的旧状态
    switch (states.get(client)) {//模拟一个状态机，根据client不同的状态有不同的转换函数，同时更新client的状态
        case 0 -> states.put(client, startScreen.respondToUserInput(e, client));
        case 1 -> states.put(client, gameScreen.respondToUserInput(e, client));
        default -> states.put(client, loseScreen.respondToUserInput(e, client));
    }
    if (old == 0 && states.get(client) == 1) {//如果玩家旧状态是0（还未开始），新状态是1（游戏中），说明当前键盘事件使得玩家开始了游戏，此时需要创建角色
    	System.out.println("create snake!!!!!!!!!!!!!!!!!!!!!");
        gameScreen.createSnake(client);//往游戏场景中新增编号为client的角色
    }
    writeDataToWritableChannels();//更新了用户状态后立刻向所有玩家发送数据
    repaint();
}
```

server向所有玩家发送信息，根据玩家的不同状态发送不同游戏场景：

```java
private void writeDataToWritableChannels() {
	for (SelectionKey key : clients) {
    	if (key.isValid()) {//判断这个key还有效。若玩家断开连接，那么旧不再给那个channel发送信息
        	int client = (int) key.attachment();//获取玩家编号
            Screen screen = switch (states.get(client)) {
                case 0 -> startScreen;
                case 1 -> gameScreen;
                default -> loseScreen;
            };//获取该玩家状态对应的Screen
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
                    ((SocketChannel) key.channel()).write(buffer);//写入给client
                }
            } catch (IOException e) {
                // 处理写入异常
            }
        }
    }
}
```

同时服务器也在不断录制游戏局面，写入到record文件夹中，同j08.





## 录屏展示

显示画面说明：

当没有玩家开始游戏时，即所有用户都在开始界面或者没有玩家连接到服务器时，服务器会显示"Waiting for player...",当有玩家在游戏中时，服务器会显示编号最小的玩家的游戏画面。当该玩家阵亡后，服务器自动寻找编号第二小的玩家，依此类推。

玩家界面则与单人模式下一致，唯一不同的是游戏内还会有其他玩家存在。

[java高级程序设计大作业j09网络通信部分：实现多人在线对战_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV14e411r7hX/?vd_source=4729de3939b68fbfb11b838e515c1fe1)