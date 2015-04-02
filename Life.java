package me.leops.life;

import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by l3ops on 31/03/2015.
 */
public class Life {
    private static Map<Pair<Integer, Integer>, Integer> world;
    private static Grid grid;
    private static Runnable simulate;
    private static JFrame window;
    private static int stepNum = 0;
    private static boolean paused = false;

    public static final int height = 66;
    public static final int width = 126;

    public static class Grid extends JPanel {

        public static final int cellSize = 15;

        private Map<Pair<Integer, Integer>, Color> cells;

        public Grid() {
            cells = new HashMap<>();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for(int i = 0; i < height; i++) {
                for(int j = 0; j < width; j++) {
                    int cellY = cellSize + (i * cellSize);
                    int cellX = cellSize + (j * cellSize);

                    Pair<Integer, Integer> pos = new Pair<>(i, j);
                    if(cells.containsKey(pos))
                        g.setColor(cells.get(pos));
                    else
                        g.setColor(Color.black);

                    g.fillRect(cellX, cellY, cellSize, cellSize);
                }
            }

            g.setColor(Color.gray);

            for (int i = cellSize; i <= width * cellSize; i += cellSize) {
                g.drawLine(i, cellSize, i, (height * cellSize) + cellSize);
            }

            for (int i = cellSize; i <= height * cellSize; i += cellSize) {
                g.drawLine(cellSize, i, (width * cellSize) + cellSize, i);
            }
        }

        public void setColor(int x, int y, Color c) {
            Pair<Integer, Integer> pos = new Pair<>(x, y);
            if(cells.putIfAbsent(pos, c) != null)
                cells.replace(pos, c);
            repaint();
        }

    }

    public static void update()  {
        window.setTitle(Integer.toString(stepNum));
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Pair<Integer, Integer> pos = new Pair<>(i, j);
                if(!world.containsKey(pos))
                    grid.setColor(i, j, Color.black);
                else {
                    Integer cell = world.get(pos);
                    if (cell.equals(1))
                        grid.setColor(i, j, Color.cyan);
                    else
                        grid.setColor(i, j, Color.blue);
                }
            }
        }
    }

    public static void step() {
        stepNum++;
        Map<Pair<Integer, Integer>, Integer> newWorld = new HashMap<>();
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int neighbors = 0;
                for(int k = i - 1; k < i + 2; k++) {
                    for(int l = j - 1; l < j + 2; l++) {
                        if((k != i || l != j) && world.containsKey(new Pair<>(k, l))) {
                            neighbors++;
                        }
                    }
                }
                Pair<Integer, Integer> pos = new Pair<>(i, j);
                if(neighbors == 3) {
                    if(!world.containsKey(pos))
                        newWorld.put(pos, 1);
                    else
                        newWorld.put(pos, world.get(pos) + 1);
                } else if(neighbors == 2 && world.containsKey(pos)) {
                    newWorld.put(pos, world.get(pos) + 1);
                }
            }
        }
        world = newWorld;
        System.gc();
    }

    public static void init() {
        stepNum = 0;
        if(world == null)
            world = new HashMap<>();
        else
            world.clear();
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                if(Math.random() > 0.7) {
                    world.put(new Pair<>(i, j), 1);
                }
            }
        }
    }

    public static void clear() {
        paused = true;
        stepNum = 0;
        world.clear();
    }

    public static void pause() {
        paused = !paused;
        if(!paused)
            simulate.run();
    }

    public static void main(String[] args) {
        init();

        grid = new Grid();
        window = new JFrame();
        window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.add(grid);
        window.setVisible(true);

        grid.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'r':
                        init();
                        if (paused) pause();
                        break;
                    case 'c':
                        clear();
                        break;
                    case 'p':
                        pause();
                        break;
                    case 's':
                        step();
                }
                update();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //
            }
        });

        grid.grabFocus();
        grid.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                Point p = e.getPoint();
                Point l = grid.getLocation();
                int x = (int) Math.floor((p.getY() - l.getY()) / Grid.cellSize) - 1;
                int y = (int) Math.floor((p.getX() - l.getX()) / Grid.cellSize) - 1;

                Pair<Integer, Integer> pos = new Pair<>(x, y);
                if (world.containsKey(pos))
                    world.remove(pos);
                else
                    world.put(pos, 1);
                update();
            }
        });

        simulate = () -> {
            step();
            update();

            try {
                Thread.sleep(25);
            } catch(InterruptedException e) {
                System.out.println(e.toString());
            } finally {
                if(!paused)
                    EventQueue.invokeLater(simulate);
            }
        };

        EventQueue.invokeLater(simulate);

    }
}
