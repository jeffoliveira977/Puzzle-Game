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
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Menu extends Component {

    public interface MenuCallback {
        void onItemSelected(String item, boolean delete);
    }

    public MenuCallback callback;

    private static final int TOP_MENU = 0;
    private static final int CENTER_MENU = 1;
    private static final int BOTTOM_MENU = 2;
    private static final int SELECTED_MENU = 3;
    private static final int REMOVE_ITEM = 4;
    private static final int REMOVE_ITEM_OVER = 5;
    public Sprite[] sprites = new Sprite[6];
    public Map<String, String> items;
    public Label label;
    public String selectedItem;
    public boolean isInsideBtn;

    Menu(Vector2i position) {
        super(position);

        try {
            this.sprites[TOP_MENU] = ImageManager.getImage("bmp_skin_buttmisc", "img_popup_0");
            this.sprites[CENTER_MENU] = ImageManager.getImage("bmp_skin_buttmisc", "img_popup_1");
            this.sprites[BOTTOM_MENU] = ImageManager.getImage("bmp_skin_buttmisc", "img_popup_2");
            this.sprites[SELECTED_MENU] = ImageManager.getImage("bmp_skin_buttmisc", "img_popup_3");
            this.sprites[REMOVE_ITEM] = ImageManager.getImage("bmp_skin_buttmisc", "img_button_x");
            this.sprites[REMOVE_ITEM_OVER] = ImageManager.getImage("bmp_skin_buttmisc", "img_button_x_ovr");
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedItem = "";
        size = new Vector2i(this.sprites[CENTER_MENU].size);
        size.x -= 6;

        items = new LinkedHashMap<>();

        label = new Label(position, "", 21);
    }

    public void addItem(String item, String key) {
        items.put(key, item);
    }

    public Object getItemValue(String item) {
        return items.get(item);
    }

    boolean contains (Point p, Vector2i position, Vector2i size) {
        return p.x >= position.x && p.x <= position.x + size.x &&
                p.y >= position.y && p.y <= position.y + size.y;
    }

    public void mouseEvent(MouseHandler handler) {
        if (this.disable && items.isEmpty())
            return;

        MouseEvent e = handler.event;
        switch (handler.type) {
            case MOUSE_PRESSED:
                if (!selectedItem.isEmpty()) {
                    onClick();
                }
                break;
            case MOUSE_RELEASED:
                if (isPressed && contains(e.getPoint())) {
                    onClick();
                } else {
                    isInside = false;
                }
                isPressed = false;
                break;
            case MOUSE_MOVED:
                int yPos = position.y + 15;
                for (var item : items.entrySet()) {
                    this.isInsideBtn = contains(e.getPoint(), new Vector2i(position.x + size.x - this.sprites[REMOVE_ITEM].size.x - 6, yPos - 3), new Vector2i(28));

                    this.isInside = contains(e.getPoint(), new Vector2i(position.x, yPos), new Vector2i(size.x - 6, size.y));
                    if (this.isInside) {
                        selectedItem = item.getKey();
                        break;
                    } else {
                        selectedItem = "";
                    }
                    yPos += 26;
                }
                break;
            default:
        }
    }

    public void render(Graphics g) {
        if (this.disable || items.isEmpty())
            return;

        this.sprites[TOP_MENU].render(g, position);
        label.position = new Vector2i(position);
        this.label.position.x += 20;
        this.sprites[BOTTOM_MENU].render(g, new Vector2i(position.x, position.y + 15 + items.size() * 26));

        List<String> keys = new ArrayList<>(items.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = items.get(key);

            int yPos = position.y + 15 + i * 26;

            if (this.selectedItem.equals(key)) {
                this.sprites[SELECTED_MENU].render(g, new Vector2i(position.x, yPos));
            } else {
                this.sprites[CENTER_MENU].render(g, new Vector2i(position.x, yPos));
            }

            if (this.isInsideBtn && this.selectedItem.equals(key)) {
                this.sprites[REMOVE_ITEM_OVER].render(g, new Vector2i(position.x + size.x - this.sprites[REMOVE_ITEM].size.x - 6, yPos + 2));
            } else {
                this.sprites[REMOVE_ITEM].render(g, new Vector2i(position.x + size.x - this.sprites[REMOVE_ITEM].size.x - 6, yPos - 3));
            }
            label.position.y = yPos + 18;
            label.text = value;
            label.render(g);
        }
    }

    public void onClick() {
        if (callback != null) {
            if (!selectedItem.isEmpty() && this.isInsideBtn) {
                for (var item : items.entrySet()) {
                    if(selectedItem.equals(item.getKey())){
                      items.remove(selectedItem);
                      break;
                    }
                }
                callback.onItemSelected(selectedItem, true);
            } else {
                callback.onItemSelected(selectedItem, false);
            }
        }
    }

    public void setCallback(MenuCallback callback) {
        this.callback = callback;
    }
}
