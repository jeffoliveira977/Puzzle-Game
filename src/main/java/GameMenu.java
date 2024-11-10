/*
 * MIT License
 * 
 * Copyright (c) 2024 - Jeff Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.joml.Vector2i;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMenu {
    private static Grid grid;
    private static Sprite backgroundSprite;
    private static Sprite splashSprite;

    private static Label label;
    private static Button[] button = new Button[3];
    private static Menu menu;
    private static Database database;
    private static boolean renderGame;
    private static String selectedSave;

    GameMenu() {

        try {
            ImageManager.parseJsonFromFile("resources\\Assets.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        backgroundSprite = ImageManager.getImage("bmp_skin_backdrop_battle", "img_backdrop_battle");
        splashSprite = ImageManager.getImage("bmp_skin_backdrop", "img_backdrop");
        selectedSave = null;

        grid = new Grid(8, 8);
        renderGame = false;

        int yPos = 90;
        try (FileInputStream file = new FileInputStream("puzzle.bin");
             DataInputStream data = new DataInputStream(file)) {
            selectedSave = data.readUTF();
            System.out.println("Selected save: " + selectedSave);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        database = new Database();
        database.create();

        if (selectedSave != null) {
            button[0] = new Button(new Vector2i(700, yPos += 60), "Continue");
            button[0].setCallback(() -> {
                load(selectedSave);
                startGame();
            });
        }

        button[1] = new Button(new Vector2i(700, yPos += 60), "New game");
        button[2] = new Button(new Vector2i(700, yPos += 60), "Load game");

        button[1].setCallback(() -> {
            int count = 0;
            if (selectedSave == null) {
                selectedSave = "save" + 0;
            } else {
                count = Integer.parseInt(selectedSave.replaceAll("[^0-9]", "")) + 1;
                // Only 10 saves
                if (count >= 10) {
                    count = 10;
                }
                selectedSave = selectedSave.replaceAll("\\d+", String.valueOf(count));
                System.out.println("saving:" + selectedSave);
            }
            ScoreBoard.playerScore[0].restart();
            ScoreBoard.playerScore[1].restart();
            save(selectedSave, count == 10);
            startGame();

            try (FileOutputStream file = new FileOutputStream("puzzle.bin");
                 DataOutputStream data = new DataOutputStream(file)) {
                data.writeUTF(selectedSave);
                load(selectedSave);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        menu = new Menu(new Vector2i(700, yPos + 60));
        menu.disable = true;

        button[2].setCallback(() -> {
            menu.disable = !menu.disable;
            List<String> saves = database.getAllSavesName();
            if (!saves.isEmpty()) {
                saves.forEach(save -> menu.addItem(save.replaceAll("(\\D)(\\d+)", "$1 $2"), save));
            }
        });

        menu.setCallback((item, delete) -> {
            if (delete) {
                System.out.println("deleted sava: " + item);
                database.deleteGems(item);
            } else {
                load(item);
                System.out.println("selected save: " + item);
                startGame();
            }
        });
    }

    private void startGame() {
        grid.generateGrid();
        renderGame = true;
        menu.disable = true;
    }

    private static void save(String saveName, boolean update) {
        Map<GemType, Integer> encodedValues = new HashMap<>();
        for (int i = 0; i < GemType.values().length; i++) {
            int gem1 = ScoreBoard.playerScore[0].gemCount.get(GemType.values()[i]);
            int gem2 = ScoreBoard.playerScore[1].gemCount.get(GemType.values()[i]);
            int encoded = gem1 | (gem2 << 0x8);
            encodedValues.put(GemType.values()[i], encoded);
        }

        int encodedHealth = ScoreBoard.playerScore[0].health |
                (ScoreBoard.playerScore[0].maxHealth << 0x8) |
                (ScoreBoard.playerScore[1].health << 0x10) |
                (ScoreBoard.playerScore[1].maxHealth << 0x18);

        if (update) {
            database.updateGems(saveName, new SaveData(encodedValues, encodedHealth));
        } else {
            database.insertGems(saveName, new SaveData(encodedValues, encodedHealth));
        }
    }

    private void load(String saveName) {
        SaveData saveData = database.getGems(saveName);

        for (GemType gemType : saveData.encodedGems.keySet()) {
            int encoded = saveData.encodedGems.get(gemType);
            int gem1 = encoded & 0xFF;
            int gem2 = (encoded >> 0x8) & 0xFF;
            ScoreBoard.playerScore[0].gemCount.put(gemType, gem1);
            ScoreBoard.playerScore[1].gemCount.put(gemType, gem2);
        }

        ScoreBoard.playerScore[0].health = (saveData.encodedHealth) & 0xFF;
        ScoreBoard.playerScore[0].maxHealth = (saveData.encodedHealth >> 0x8) & 0xFF;
        ScoreBoard.playerScore[1].health = (saveData.encodedHealth >> 0x10) & 0xFF;
        ScoreBoard.playerScore[1].maxHealth = (saveData.encodedHealth >> 0x18) & 0xFF;
    }

    public static void update(double dt) {
        if (renderGame) {
            grid.update(dt);
        } else {
            for (var btn : button) {
                if (btn != null) {
                    btn.update(dt);
                }
            }
            button[2].disable = selectedSave == null;
            menu.update(dt);
        }
    }

    public static void render(Graphics g) {
        if (renderGame) {
            backgroundSprite.render(g, 0, 0, 1024, 768);
            grid.render(g);
        } else {
            splashSprite.render(g, 0, 0, 1024, 768);
            for (var btn : button) {
                if (btn != null) {
                    btn.render(g);
                }
            }
            menu.render(g);
        }
    }

    public static void mouseEvent(MouseHandler handler) {
        if (renderGame) {
            grid.mouseEvent(handler);
        } else {
            for (var btn : button) {
                if (btn != null) {
                    btn.mouseEvent(handler);
                }
            }
            menu.mouseEvent(handler);
        }
    }

    public static void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            renderGame = !renderGame;
            System.out.println("updating: " + selectedSave);
            save(selectedSave, true);
        }
    }
}
