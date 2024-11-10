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

public class AnimatedSprite {
    private List<Sprite> frames;
    private int currentFrame;
    private double  timer;
    private final double speed;
    public Vector2i size;

    public AnimatedSprite(List<Sprite> frames, int speed) {
        this.speed = (double) speed / 1000;

        this.frames = frames;

        currentFrame = 0;
        timer = 0;
        size = frames.getFirst().size;
    }

    public void update(double dt) {
        timer += dt;
        if (timer > speed) {
            currentFrame++;
            timer = 0;

            if (currentFrame >= frames.size()) {
                currentFrame = 0;
            }
        }
    }

    public void render(Graphics g, Vector2i position) {
        frames.get(currentFrame).render(g, position);
    }

    public void render(Graphics g, Vector2i position, Vector2i size) {
        frames.get(currentFrame).render(g, position, size);
    }
}
