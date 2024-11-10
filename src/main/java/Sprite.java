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

import org.joml.Math;
import org.joml.Vector2i;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Sprite {

    public BufferedImage image;
    public int angle;
    public float alpha;
    public Vector2i pos;
    public Vector2i size;

    public Sprite() {
        this(null);
    }

    public Sprite(BufferedImage image) {
        this(image, new Vector2i(), new Vector2i());
    }

    public Sprite(BufferedImage image, Vector2i pos, Vector2i size) {
        this(image, pos.x, pos.y, size.x, size.y);
    }

    public Sprite(BufferedImage image, int x, int y, int w, int h) {
        this.image = image;
        this.alpha = 1.0f;
        this.angle = 0;
        this.pos = new Vector2i(x, y);
        this.size = new Vector2i(w, h);
    }

    public void render(Graphics g) {
        this.render(g, this.pos, this.size);
    }

    public void render(Graphics g, Vector2i pos) {
        this.render(g, pos.x, pos.y, size.x, size.y);
    }

    public void render(Graphics g, Vector2i pos, Vector2i size) {
        this.render(g, pos.x, pos.y, size.x, size.y);
    }

    public void render(Graphics g, int x, int y, int w, int h) {

        if (this.angle == 0) {
            Graphics2D g2d = (Graphics2D) g;

            Composite composite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(this.image, x, y, w, h, null);
            g2d.setComposite(composite);
            return;
        }

        int origWidth = this.image.getWidth(null);
        int origHeight = this.image.getHeight(null);

        BufferedImage rotated = new BufferedImage(origWidth, origHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.translate((double) origWidth / 2, (double) origHeight / 2);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawRenderedImage(this.image, AffineTransform.getTranslateInstance(-origWidth / 2.0, -origHeight / 2.0));
        g2d.dispose();

        // Center the rotated image
        int newX = x - (w - origWidth) / 2;
        int newY = y - (h - origHeight) / 2;
        g.drawImage(rotated, newX, newY, w, h, null);
    }
}
