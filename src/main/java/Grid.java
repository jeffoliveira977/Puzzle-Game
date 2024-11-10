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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Grid {
    public static final int blockSize = 71; // Tamanho do bloco do grid
    public static final int gap = 3; // Espaço entre os blocos do grid
    public static final Vector2i offset = new Vector2i(217, 133);
    public static Vector2i size;
    private final Sprite[] selectorSprite = new Sprite[2];
    private ArrayList<Gem> table;
    private final int numRows;
    private final int numColumns;
    private double animTime;
    private float selectorAlpha;
    private float messageAlpha;

    static boolean isSwappingBack;
    private boolean animateSelector;
    private boolean firstGemClicked;

    private Vector2i selectorSpriteSize;
    private Vector2i firstGemSelectedCoord;
    private Vector2i secondGemSelectedCoord;
    private Vector2i dragStartCoord;

    private Map<String, Sprite> gemsSprites;
    private List<GemMatch> matches;
    private GemSwap gemSwaps;
    private static ScoreBoard scoreBoard;
    private static boolean generatingGrid;
    private Label message;
    private Label playerWinsText;
    private Button restartBtn;
    private Map<Gem, GemType> gemsReplace = new HashMap<>();
    private int curSelector;

    Grid(int x, int y) {
        this.numRows = x;
        this.numColumns = y;
        this.table = new ArrayList<>(x * y);
        this.curSelector = 0;

        Grid.size = new Vector2i(numRows * (Grid.blockSize + Grid.gap),
                numColumns * (Grid.blockSize + Grid.gap));

        this.selectorAlpha = 1.0f;
        this.firstGemSelectedCoord = new Vector2i();
        this.secondGemSelectedCoord = new Vector2i();
        this.dragStartCoord = new Vector2i();
        this.gemsSprites = new HashMap<>();
        this.matches = new ArrayList<>();
        this.gemSwaps = null;

        try {
            var bitmapSelector = ImageManager.getBitmapDataList("bmp_skin_battlemisc");
            this.selectorSprite[0] = bitmapSelector.get(1).sprite;
            this.selectorSprite[1] = bitmapSelector.get(0).sprite;
            this.selectorSpriteSize = new Vector2i(selectorSprite[0].size);

            var bitmapGems = ImageManager.getBitmapDataList("bmp_skin_gemsgrid");
            bitmapGems.forEach(item -> {
                String imgName = item.name.replace("img_", "");
                for (GemType gemType : GemType.values()) {
                    if (gemType.name().toLowerCase().contains(imgName)) {
                        this.gemsSprites.put(imgName, item.sprite);
                        break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        message = new Label(new Vector2i(365, 450), "No moves available", 40);
        playerWinsText = new Label(new Vector2i(106, 260), "", 62);
        restartBtn = new Button(new Vector2i(422, 420), "Restart game");
        restartBtn.size.x = 180;
        restartBtn.disable = true;

        restartBtn.setCallback(() -> {
            generateGrid();
            enablePlayerWinnerDraw = false;
            ScoreBoard.playerScore[0].restart();
        });
        scoreBoard = new ScoreBoard();
    }

    private Gem getGem(Vector2i vec) {
        return this.table.get(vec.x * numColumns + vec.y);
    }

    private Gem getGem(int row, int column) {
        return this.table.get(row * numColumns + column);
    }

    private void setGem(int row, int column, Gem gem) {
        this.table.add(row * numColumns + column, gem);
    }

    public void generateGrid() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                int index = MathUtils.randInt(0, GemType.values().length);
                GemType type = GemType.values()[index];
                Sprite sprite = this.gemsSprites.get(type.name().toLowerCase());

                Gem gem = new Gem();
                gem.setType(type);
                gem.setSprite(sprite.image);
                gem.setCoord(i, j);
                gem.pos.x = i * (Grid.blockSize + Grid.gap);
                gem.pos.y = j * (Grid.blockSize + Grid.gap + gem.randY / 2) - Grid.size.y;
                gem.moveDown(j * (Grid.blockSize + Grid.gap), false, 1.0);
                this.setGem(i, j, gem);
            }
        }
        firstGemSelectedCoord.set(-1);
        secondGemSelectedCoord.set(-1);
        ScoreBoard.currentPlayer = 0;
        generatingGrid = true;
        animTime = 0;
    }

    // Checks if two blocks are adjacent to each other on the grid.
    private boolean isAdjacent(Vector2i coordA, Vector2i coordB) {
        int dX = Math.abs(coordA.x - coordB.x);
        int dY = Math.abs(coordA.y - coordB.y);
        return (dX + dY == 1);
    }

    // Calculates a grid block position from pixel coordinates and gem size
    private Vector2i getBlockPosition(Vector2i coords, Vector2i size) {
        int x = (Grid.offset.x - size.x / 2 + Grid.blockSize / 2) + coords.x * (Grid.blockSize + Grid.gap);
        int y = (Grid.offset.y - size.y / 2 + Grid.blockSize / 2) + coords.y * (Grid.blockSize + Grid.gap);
        return new Vector2i(x, y);
    }

    private boolean trySwap(Vector2i coordA, Vector2i coordB) {
        swap(coordA, coordB);

        if (this.findMatches(true)) {
            return true;
        }

        swap(coordA, coordB);
        return false;
    }

    private boolean canSwap(Vector2i coordA, Vector2i coordB, boolean addToScore) {
        swap(coordA, coordB);
        boolean match = this.findMatches(false);
        swap(coordA, coordB);
        return match;
    }

    private void swap(Vector2i coordA, Vector2i coordB) {
        Gem gemA = this.getGem(coordA);
        Gem gemB = this.getGem(coordB);

        GemType type = gemA.gemType;
        BufferedImage sprite = gemA.sprite;
        gemA.setType(gemB.gemType);
        gemA.setSprite(gemB.sprite);
        gemB.setType(type);
        gemB.setSprite(sprite);
    }

    private boolean findAvailableMoves() {
        for (int x = 0; x < numRows - 1; x++) {
            for (int y = 0; y < numColumns; y++) {
                if (this.canSwap(new Vector2i(x, y), new Vector2i(x + 1, y), false)) {
                    return true;
                }
            }
        }

        for (int y = 0; y < numColumns - 1; y++) {
            for (int x = 0; x < numRows; x++) {
                if (this.canSwap(new Vector2i(x, y), new Vector2i(x, y + 1), false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void addGemsToScore(int r, int c, int length) {
        if (this.getGem(r, c).gemType == GemType.GEM_GREEN) {
            fillGemsWith(GemType.GEM_SKULL, GemType.GEM_RED);
        } else if (this.getGem(r, c).gemType == GemType.GEM_BLUE) {
            fillGemsWith(GemType.GEM_RED, GemType.GEM_SKULL);
        }
        System.out.println("added gems: " + length);
        scoreBoard.addGem(this.getGem(r, c).gemType, length);
    }

    // Scans for and records sequences of three or more identical gems by rows and columns.
    private boolean findMatches(boolean addToScore) {
        this.matches.clear();

        //Check horizontal matches
        for (int i = 0; i < numRows; i++) {
            int length = 1;
            for (int j = 0; j < numColumns; j++) {
                if (j < numColumns - 1 && this.getGem(i, j).isEqual(this.getGem(i, j + 1))) {
                    length++;
                } else {
                    // Add to the list of match if 3 or more identical gems were found in a row.
                    if (length >= 3) {
                        for (int k = 0; k < length; k++) {
                            this.matches.add(new GemMatch(new Vector2i(i, j - k), length));
                        }
                        if (addToScore) {
                            addGemsToScore(i, j, length);
                        }
                    }
                    length = 1;
                }
            }
        }

        // Check vertical matches
        for (int j = 0; j < numColumns; j++) {
            int length = 1;
            for (int i = 0; i < numRows; i++) {
                if (i < numRows - 1 && this.getGem(i, j).isEqual(this.getGem(i + 1, j))) {
                    length++;
                } else {
                    // Add to the list of match if 3 or more identical gems were found in a row.
                    if (length >= 3) {
                        for (int k = 0; k < length; k++) {
                            this.matches.add(new GemMatch(new Vector2i(i - k, j), length));
                        }
                        if (addToScore) {
                            addGemsToScore(i, j, length);
                        }
                    }
                    length = 1;
                }
            }
        }
        return !this.matches.isEmpty();
    }

    // Remove all matching gems.
    private void removeMatchingGems() {
        for (var match : matches) {
            this.getGem(match.position).setType(null);
            this.getGem(match.position).fadeOut();
        }
    }

    // Gets a list of dropping gems.
    private List<GemDrop> getDroppingGems() {
        List<GemDrop> dropList = new ArrayList<>();
        for (int x = 0; x < numRows; x++) {
            int drop = 0;
            for (int y = numColumns - 1; y >= 0; y--) {

                if (!this.getGem(x, y).isFadeOutComplete())
                    continue;

                if (this.getGem(x, y).gemType == null) {
                    dropList.add(new GemDrop(x, y, 0));
                    drop++;
                } else if (drop > 0) {
                    dropList.add(new GemDrop(x, y, drop));
                }
            }
        }

        return dropList;
    }

    private boolean isGemsFinishedAnim() {
        return table.stream().anyMatch(gem ->
                !gem.hasMovedDown() || !gem.isFadeOutComplete() || !gem.hasSwapped()
        );
    }

    // Drop and insert new gems.
    private void dropGems(double dt) {
        if (this.isGemsFinishedAnim()) {
            return;
        }

        var dropList = this.getDroppingGems();
        for (GemDrop drop : dropList) {
            Vector2i coord = new Vector2i(drop.x, drop.y + drop.shift);

            // Insert new gems
            int index = MathUtils.randInt(0, GemType.values().length);
            GemType type = GemType.values()[index];
            Sprite sprite = this.gemsSprites.get(type.name().toLowerCase());

            this.getGem(coord).setType(type);
            this.getGem(coord).setSprite(sprite.image);
            this.getGem(coord).alpha = 1f;
            this.getGem(coord).pos.y = (coord.y - 1) * (Grid.blockSize + Grid.gap);
            this.getGem(coord).moveDown((coord.y) * (Grid.blockSize + Grid.gap), false, 0.2);

            // Swap gems
            if (drop.shift > 0) {
                this.getGem(new Vector2i(drop.x, drop.y)).alpha = 1f;
                swap(new Vector2i(drop.x, drop.y), coord);
                this.getGem(new Vector2i(drop.x, drop.y)).pos.y = (drop.y - 1) * (Grid.blockSize + Grid.gap);
                this.getGem(new Vector2i(drop.x, drop.y)).moveDown(drop.y * (Grid.blockSize + Grid.gap), false, 0.2);
            }
        }
    }

    // Processes and executes gems swap actions if matches.
    private void processGemsSwaps() {
        if (gemSwaps != null) {
            Gem gemA = gemSwaps.gemA;
            Gem gemB = gemSwaps.gemB;

            // Wait for the gems to complete their animations before swapping them.
            if (gemSwaps.canSwap()) {

                if (isAdjacent(gemA.coord, gemB.coord)) {
                    if (trySwap(gemA.coord, gemB.coord)) {
                        // We need to swap the position of the gems too.
                        Vector2i temp = new Vector2i(gemA.pos);
                        gemA.pos = new Vector2i(gemB.pos);
                        gemB.pos = temp;
                        gemSwaps = null;
                    } else {
                        // If the sequence doesn't match, then swap it back.
                        if (!isSwappingBack) {
                            gemA.move(gemB);
                            gemB.move(gemA);
                            isSwappingBack = true;
                        }
                    }
                }
            }
        }
        if (isSwappingBack && (gemSwaps != null && gemSwaps.canSwap())) {
            isSwappingBack = false;
            gemSwaps = null;
        }
    }

    private void processSelector(double dt) {
        if (animateSelector) {
            animTime += dt;
            int scale = (int) (animTime * 4);
            selectorSpriteSize.add(scale, scale);
            selectorAlpha = (float) Math.clamp(1f - animTime / 1.2f, 0, 1);
            if (selectorAlpha <= 0.0f) {
                firstGemSelectedCoord.set(-1);
                secondGemSelectedCoord.set(-1);
                animateSelector = false;
                animTime = 0.0f;
                curSelector = ScoreBoard.currentPlayer;
            }
        } else {
            selectorAlpha = 1.0f;
            selectorSpriteSize = new Vector2i(selectorSprite[0].size);
        }
    }

    private void destroyAllGems() {
        System.out.println("destroyed");
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                this.getGem(i, j).pos.x = i * (Grid.blockSize + Grid.gap);
                this.getGem(i, j).pos.y = j * (Grid.blockSize + Grid.gap);
                this.getGem(i, j).moveDown(Grid.size.y + Grid.blockSize + Grid.gap, true, 1.5);
            }
        }
    }

    private void fillGemsWith(GemType from, GemType to) {
        table.forEach(gem -> {
            if (gem.gemType == from) {
                gem.fadeOut();
                gemsReplace.put(gem, to);
            }
        });
    }

    private boolean enablePlayerWinnerDraw = false;

    private void drawPlayerWinner(Graphics g) {
        if (!enablePlayerWinnerDraw)
            return;

        g.setColor(new Color(0, 0, 0, 0.4f));
        g.fillRect(Grid.offset.x, Grid.offset.y, Grid.size.x, Grid.size.y);
        playerWinsText.text = ScoreBoard.playerScore[0].health > 0 ? "Player 1 wins" : "Player 2 wins";
        playerWinsText.color = Color.CYAN;
        playerWinsText.position = new Vector2i(355, 400);
        playerWinsText.backgroundWidth = 20;
        playerWinsText.backgroundColor = new Color(0, 0, 0, 0.8f);
        playerWinsText.outlineWidth = 2;
        playerWinsText.render(g);
        restartBtn.render(g);
    }

    private void drawMessage(Graphics g) {
        if (messageAlpha == 0)
            return;

        messageAlpha = Math.clamp(messageAlpha, 0.0f, 1.0f);
        float opacity = Math.min(0.7f, messageAlpha);
        message.backgroundColor = new Color(0, 0, 0, opacity);
        message.backgroundWidth = 10;
        message.color = Color.cyan;
        message.color = new Color(message.color.getRed(), message.color.getGreen(), message.color.getBlue(), (int) (messageAlpha * 255));
        message.outlineWidth = 5;
        message.render(g);
    }

    private boolean hasGemsDestroyed;

    private void availableGems(double dt) {
        if (!this.isGemsFinishedAnim()) {
            boolean availableMoves = this.findAvailableMoves();
            if (!availableMoves && !hasGemsDestroyed) {
                System.out.println("no valid moves");
                this.destroyAllGems();
                hasGemsDestroyed = true;
                messageAlpha = 1.0f;
                generatingGrid = true;
                System.out.println("there no gems available.");
            }
            if (generatingGrid) {
                messageAlpha -= (int) (dt);
                System.out.println(messageAlpha);
            }

            if (hasGemsDestroyed && messageAlpha <= 0.5f) {
                this.generateGrid();
                hasGemsDestroyed = false;
            }
        }
    }

    public void update(double dt) {
        table.forEach(gem -> gem.update(dt));

        if (generatingGrid) {
            generatingGrid = this.getGem(0, 1).hasMovedDown();
        } else {
            this.findMatches(false);
            this.removeMatchingGems();
            this.dropGems(dt);
            this.processGemsSwaps();
            this.processSelector(dt);
            this.availableGems(dt);
        }
        scoreBoard.update(dt);
        restartBtn.update(dt);

        boolean animFinished = false;
        for (var item : gemsReplace.entrySet()) {
            Gem gem = item.getKey();
            if (!gem.isFadeOutComplete()) {
                animFinished = true;
                continue;
            }
            GemType type = item.getValue();
            gem.setType(type);
            gem.alpha = 1.0f;
            Sprite sprite = this.gemsSprites.get(type.name().toLowerCase());
            item.getKey().setSprite(sprite.image);
        }

        if (!animFinished) {
            gemsReplace.clear();
        }

        if (scoreBoard.anyPlayerWinner()) {
            if (!enablePlayerWinnerDraw) {
                this.destroyAllGems();
                restartBtn.disable = false;
                enablePlayerWinnerDraw = true;
                System.out.println("destroyed gems");
            }
        }
    }

    public void render(Graphics g) {
        table.forEach(gem -> gem.draw(g));

        if (firstGemSelectedCoord.x > -1 && firstGemSelectedCoord.y > -1) {
            Vector2i coords = this.getBlockPosition(firstGemSelectedCoord, selectorSprite[curSelector].size);
            selectorSprite[curSelector].angle = (selectorSprite[curSelector].angle + 1) % 360;
            selectorSprite[curSelector].alpha = selectorAlpha;
            selectorSprite[curSelector].render(g, coords, selectorSpriteSize);
            if (secondGemSelectedCoord.x > -1 && secondGemSelectedCoord.y > -1) {
                coords = this.getBlockPosition(secondGemSelectedCoord, selectorSprite[curSelector].size);
                selectorSprite[curSelector].render(g, coords, selectorSpriteSize);
            }
        }
        scoreBoard.render(g);
        drawMessage(g);
        drawPlayerWinner(g);
    }

    private Vector2i getCoordFromMousePos(MouseEvent e) {
        int x = (e.getX() - Grid.offset.x) / (Grid.blockSize + Grid.gap);
        int y = (e.getY() - Grid.offset.y) / (Grid.blockSize + Grid.gap);

        x = Math.clamp(0, x, numRows - 1);
        y = Math.clamp(0, y, numColumns - 1);
        return new Vector2i(x, y);
    }

    private boolean isMouseHover(MouseEvent e) {
        return (e.getX() >= Grid.offset.x && e.getX() < Grid.offset.x + numRows * (Grid.blockSize + Grid.gap)) &&
                (e.getY() >= Grid.offset.y && e.getY() < Grid.offset.y + numColumns * (Grid.blockSize + Grid.gap));
    }

    public void mouseEvent(MouseHandler handler) {
        MouseEvent e = handler.event;

        restartBtn.mouseEvent(handler);

        if (!this.isMouseHover(e))
            return;

        if (this.isGemsFinishedAnim())
            return;

        Vector2i gridCoords = this.getCoordFromMousePos(e);

        switch (handler.type) {
            case MOUSE_PRESSED:

                if (firstGemClicked) {
                    if (firstGemSelectedCoord.equals(gridCoords)) {
                        firstGemClicked = false;
                        firstGemSelectedCoord.set(-1);
                    } else {
                        if (isAdjacent(firstGemSelectedCoord, gridCoords)) {
                            boolean match = this.canSwap(firstGemSelectedCoord, gridCoords, false);
                            this.getGem(firstGemSelectedCoord).move(this.getGem(gridCoords));
                            this.getGem(gridCoords).move(this.getGem(firstGemSelectedCoord));
                            gemSwaps = new GemSwap(this.getGem(firstGemSelectedCoord), this.getGem(gridCoords));
                            if (!match) {
                                firstGemSelectedCoord.set(-1);
                                secondGemSelectedCoord.set(-1);
                                return;
                            }
                            secondGemSelectedCoord = gridCoords;
                            animateSelector = true;
                            firstGemClicked = false;
                            System.out.println("move gem");
                        } else {
                            firstGemSelectedCoord = gridCoords;
                            secondGemSelectedCoord.set(-1);
                            System.out.println("out gem");
                        }
                    }
                } else {
                    firstGemSelectedCoord = gridCoords;
                    secondGemSelectedCoord.set(-1);
                    firstGemClicked = true;
                    System.out.println("first gem clicked");
                }
                dragStartCoord = gridCoords;
                break;
            case MOUSE_DRAGGED:
                if (isAdjacent(dragStartCoord, gridCoords)) {
                    this.getGem(dragStartCoord).move(this.getGem(gridCoords));
                    this.getGem(gridCoords).move(this.getGem(dragStartCoord));
                    firstGemSelectedCoord = new Vector2i(dragStartCoord);
                    gemSwaps = new GemSwap(this.getGem(dragStartCoord), this.getGem(gridCoords));
                    dragStartCoord.set(-1);
                    firstGemClicked = false;
                    animateSelector = true;
                }
                break;
            default:
        }
    }
}
