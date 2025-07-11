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

import java.util.HashMap;
import java.util.Map;

class PlayerScore {
    public Map<GemType, Integer> gemCount = new HashMap<>();
    int health;
    int maxHealth;
    boolean nextTurn;

    PlayerScore() {
        gemCount = new HashMap<>();
        this.restart();
    }

    public void restart(){
        for (GemType gem : GemType.values()) {
            gemCount.put(gem, 0);
        }
        health = maxHealth = 50;
    }

    public void clearGem(GemType gem) {
        gemCount.put(gem, 0);
    }

    public void addGem(GemType gem, int count) {
        gemCount.put(gem, gemCount.get(gem) + count);
    }

    public int getGemCount(GemType gem) {
        return gemCount.get(gem);
    }
}
