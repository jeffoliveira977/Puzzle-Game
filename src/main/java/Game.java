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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.concurrent.TimeUnit;

public class Game extends Canvas implements Runnable, KeyListener {
    private static BufferStrategy bs;
    private static Thread thread;
    private static final long TIME_FRAME = 1000 / 60;
    private static JFrame frame = null;
    private static long lastFrameTime;
    private static boolean isRunning = true;

    Game() {
        new GameMenu();
        MouseEvents mouseEvents = new MouseEvents();
        this.addMouseListener(mouseEvents);
        this.addMouseMotionListener(mouseEvents);
        this.addKeyListener(this);
    }

    public void run() {
        createBufferStrategy(3);
        bs = getBufferStrategy();

        requestFocus();

        lastFrameTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (isRunning) {
            do {
                do {
                    long elapsedTime = currentTime - lastFrameTime;

                    Graphics g = bs.getDrawGraphics();
                    render(g);
                    g.dispose();

                    while (elapsedTime < TIME_FRAME) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(TIME_FRAME - elapsedTime);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Uncaught", e);
                        }
                 
                        currentTime = System.currentTimeMillis();
                        elapsedTime = currentTime - lastFrameTime;

                        double elapsed = elapsedTime / 1000.0f;
                        update(elapsed);
                        lastFrameTime = System.currentTimeMillis();
                    }
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        }
        bs.dispose();
    }

    public void update(double dt) {
        GameMenu.update(dt);
    }

    public void render(Graphics g) {
        GameMenu.render(g);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {GameMenu.keyPressed(e);}

    @Override
    public void keyReleased(KeyEvent e) {}

    public synchronized void start() {
        thread = new Thread(this, "Display");
        thread.start();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setBackground(Color.BLACK);
        frame = new JFrame("Puzzle Quest");
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(1038, 788);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.setAlwaysOnTop(false);

        game.start();
    }
}
