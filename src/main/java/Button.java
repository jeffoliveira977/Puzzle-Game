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

public class Button extends Component {
    public Sprite actionSprte;
    public Sprite pressedSprite;
    public AnimatedSprite animatedSprite;
    public String text;
    public Label label;

    Button(Vector2i position, String text, Vector2i size) {
        super(position);
        this.loadTextures();

        this.size = size;
        this.text = text;
        this.label = new Label(new Vector2i(), text, 22);
    }

    Button(Vector2i position, String text) {
        super(position);

        this.loadTextures();

        this.size = new Vector2i(this.actionSprte.size);
        this.text = text;
        this.label = new Label(new Vector2i(), text, 22);
    }

    private void loadTextures() {
        try {
            this.actionSprte = ImageManager.getImage("bmp_skin_buttmain", "img_buttwide_act");
            this.pressedSprite = ImageManager.getImage("bmp_skin_buttmain", "img_buttwide_pres");
            this.size = this.actionSprte.size;

            var spritesList = ImageManager.getSpritesInRange("bmp_skin_buttmain",
                    "img_buttwide_hi0",
                    "img_buttwide_hi3");

            this.animatedSprite = new AnimatedSprite(spritesList, 30);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(double dt) {
        if(this.disable) return;
        this.animatedSprite.update(dt);
    }

    public void render(Graphics g) {
        if(this.disable) return;
        this.label.position = new Vector2i(this.position);

        if (this.isPressed) {
            this.pressedSprite.render(g, this.position, this.size);
            this.label.position.y++;
        } else if (this.isInside) {
            this.animatedSprite.render(g, this.position, this.size);
        } else {
            this.actionSprte.render(g, this.position, this.size);
        }

        FontMetrics metrics = g.getFontMetrics(this.label.font);
        int width = metrics.stringWidth(text);
        int height = metrics.getHeight();
        int ascent = metrics.getAscent();

        // Align the text on the button center.
        this.label.position.x += (this.size.x - width) / 2;
        this.label.position.y += (this.size.y - height) / 2 + ascent;

        this.label.text = this.text;
        this.label.outlineWidth = 2;
        this.label.render(g);
    }
}
