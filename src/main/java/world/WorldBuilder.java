package world;

import java.io.*;
import java.util.Random;

public class WorldBuilder implements Serializable {

    private int width;
    private int height;
    private Tile[][] tiles;

    public WorldBuilder(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        String fileName = "/map.txt";

        try (InputStream inputStream = WorldBuilder.class.getResourceAsStream(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] numbers = line.split(" ");
                for (int j = 0; j < numbers.length; j++) {
                    switch (numbers[j]) {
                        case "0":
                            tiles[i][j] = Tile.FLOOR;
                            break;
                        case "1":
                            tiles[i][j] = Tile.WALL;
                            break;
                        case "2":
                            tiles[i][j] = Tile.GRASS;
                            break;
                        case "3":
                            tiles[i][j] = Tile.WATER;
                            break;
                        case "4":
                            tiles[i][j] = Tile.UPPER_LEFT;
                            break;
                        case "5":
                            tiles[i][j] = Tile.UPPER_RIGHT;
                            break;
                        case "6":
                            tiles[i][j] = Tile.LOWER_LEFT;
                            break;
                        case "7":
                            tiles[i][j] = Tile.LOWER_RIGHT;
                            break;
                        case "8":
                            tiles[i][j] = Tile.ROW;
                            break;
                        case "9":
                            tiles[i][j] = Tile.COL;
                            break;
                    }
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public World build() {
        return new World(tiles);
    }
}
