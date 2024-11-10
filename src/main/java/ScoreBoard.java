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
import java.util.List;
import java.util.Map;

public class ScoreBoard {
    private Label gemsText;
    public static int currentPlayer;
    public static PlayerScore[] playerScore = new PlayerScore[2];
    public Sprite playerHealthBar;
    public Sprite infoBarBg;
    public List<Sprite> infoBarList;
    public AnimatedSprite shieldGlowSprite;
    public AnimatedSprite[] shieldSprite = new AnimatedSprite[2];

    ScoreBoard() {
        gemsText = new Label(new Vector2i(106, 260), "", 16);
        playerScore[0] = new PlayerScore();
        playerScore[1] = new PlayerScore();

        currentPlayer = 0;

        try {
            this.playerHealthBar = ImageManager.getImage("bmp_skin_battlemisc", "img_selglow");
            this.infoBarBg = ImageManager.getImage("bmp_skin_battlemisc", "img_gems_bar_bk");

            infoBarList = ImageManager.getSpritesInRange("bmp_skin_battlemisc",
                    "img_gems_bar_grn",
                    "img_gems_bar_blu");

            var spritesList = ImageManager.getSpritesInRange("bmp_skin_battlemisc",
                    "img_shieldgrn_0",
                    "img_shieldgrn_8");

            this.shieldSprite[0] = new AnimatedSprite(spritesList, 60);

            spritesList = ImageManager.getSpritesInRange("bmp_skin_battlemisc",
                    "img_shieldred_0",
                    "img_shieldred_8");

            this.shieldSprite[1] = new AnimatedSprite(spritesList, 60);

            spritesList = ImageManager.getSpritesInRange("bmp_skin_gemsgrid",
                    "img_shieldglow_0",
                    "img_shieldglow_3");

            this.shieldGlowSprite = new AnimatedSprite(spritesList, 220);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addGem(GemType gemType, int length) {
        if (gemType == null || length == 0) {
            return;
        }

        int totalGems = 0;
        int invertPlayer = currentPlayer > 0 ? 0 : 1;

        // If a set of 3 skull gems is collected, reduce the opponent's health (one per gem)
        if (length >= 3 && gemType.equals(GemType.GEM_SKULL))
            playerScore[invertPlayer].health -= 3;

        // If a set of 3 yellow gems is collected, clear the opponent's gold
        if (length >= 3 && gemType.equals(GemType.GEM_YELLOW))
            playerScore[invertPlayer].clearGem(gemType);

        // If a set of 3 red gems is collected, increase the player's health (one per gem)
        if (length >= 3 && gemType.equals(GemType.GEM_RED))
            playerScore[currentPlayer].health += 3;

        // After collecting 10 experience gems, reduce the opponent's max health and clear the player's experience
        totalGems = playerScore[currentPlayer].getGemCount(GemType.GEM_EXP);
        if (totalGems >= 10) {
            playerScore[currentPlayer].clearGem(gemType);
            playerScore[invertPlayer].maxHealth -= 3;
        }

        // After collecting 10 gold gems, reduce the opponent's health in the next turn and reset the player's gold
        totalGems = playerScore[currentPlayer].getGemCount(GemType.GEM_GOLD);
        if (totalGems >= 10) {
            if (playerScore[currentPlayer].nextTurn) {
                playerScore[currentPlayer].clearGem(GemType.GEM_GOLD);
                playerScore[currentPlayer].nextTurn = false;
                playerScore[invertPlayer].health -= 3;
            }
            playerScore[currentPlayer].nextTurn = true;
        }

        playerScore[currentPlayer].addGem(gemType, length);

        //Do not change the turn if the player creates a sequence of 4 gems of the same color.
        if (length <= 4) {
            currentPlayer = currentPlayer > 0 ? 0 : 1;
        }
    }

    public boolean anyPlayerWinner () {
        return playerScore[0].health == 0 || playerScore[1].health == 0;
    }

    public void update(double dt) {
        playerScore[0].maxHealth = Math.max(playerScore[0].maxHealth, 5);
        playerScore[1].maxHealth = Math.max(playerScore[1].maxHealth, 5);
        playerScore[0].health = Math.clamp(playerScore[0].health, 0, playerScore[0].maxHealth);
        playerScore[1].health = Math.clamp(playerScore[1].health, 0, playerScore[1].maxHealth);

        this.shieldSprite[0].update(dt);
        this.shieldSprite[1].update(dt);
        this.shieldGlowSprite.update(dt);
    }

    private void drawScore(Graphics g, Vector2i position, Color color, int score) {
        gemsText.text = String.valueOf(score);
        gemsText.color = color;
        gemsText.position = position;
        gemsText.render(g);
    }

    private void drawPlayerScore(Graphics g, int playerID, int posX) {
        int score = playerScore[playerID].getGemCount(GemType.GEM_GOLD);
        drawScore(g, new Vector2i(posX, 288), Color.YELLOW, score);
        score = playerScore[playerID].getGemCount(GemType.GEM_EXP);
        drawScore(g, new Vector2i(posX + 73, 288), Color.MAGENTA, score);

        infoBarBg.render(g, new Vector2i(posX + 59, 168));

        for (int i = 0; i < 4; i++) {
            score = playerScore[playerID].getGemCount(GemType.values()[i]);
            int width = infoBarList.get(i).size.x;
            int height = infoBarList.get(i).size.y;
            int progress = score * height / 100;
            infoBarList.get(i).render(g, new Vector2i(posX + 59, 168 + height - progress), new Vector2i(width, progress));
            posX += 21;
        }

        for (int i = 0; i < 4; i++) {
            score = playerScore[playerID].getGemCount(GemType.values()[i]);
            drawScore(g, new Vector2i(posX - 21, 260), Color.WHITE, score);
            posX += 21;
        }
    }

    private void drawPlayerHealth(Graphics g, int playerID, int posX) {
        int health = playerScore[playerID].health;
        int maxHealth = playerScore[playerID].maxHealth;
        int progress = health * this.playerHealthBar.size.x / maxHealth;
        Vector2i position = new Vector2i(posX, 123);
        this.playerHealthBar.render(g, position, new Vector2i(progress, this.playerHealthBar.size.y));

        FontMetrics metrics = g.getFontMetrics(this.gemsText.font);
        this.gemsText.text = health + " de " + maxHealth;
        int width = metrics.stringWidth(this.gemsText.text);
        int height = metrics.getHeight();
        int ascent = metrics.getAscent();

        // Align the text on the progress bar center.
        this.gemsText.position.x = position.x + (this.playerHealthBar.size.x - width) / 2;
        this.gemsText.position.y = position.y + (this.playerHealthBar.size.y - height) / 2 + ascent;

        this.gemsText.color = Color.WHITE;
        this.gemsText.shadowOffset = 0;
        this.gemsText.render(g);
    }

    private void drawShield(Graphics g) {
        if (this.anyPlayerWinner()) {
            return;
        }

        Vector2i position = currentPlayer == 0 ?
                new Vector2i(84, 70) :
                new Vector2i(900, 70);

        Vector2i spriteSize = this.shieldSprite[currentPlayer].size;
        Vector2i newSize = new Vector2i(130, 130);
        this.shieldGlowSprite.render(g, new Vector2i(
                position.x - (newSize.x - spriteSize.x) / 2,
                position.y - (newSize.x - spriteSize.y) / 2), newSize);

        this.shieldSprite[currentPlayer].render(g, position);
    }

    void render(Graphics g) {
        gemsText.shadowOffset = 2;

        drawPlayerScore(g, 0, 44);
        drawPlayerScore(g, 1, 868);

        drawPlayerHealth(g, 0, 15);
        drawPlayerHealth(g, 1, 838);
        drawShield(g);
    }
}
