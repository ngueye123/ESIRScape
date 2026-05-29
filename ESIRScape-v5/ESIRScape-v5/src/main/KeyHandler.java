package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean spacePressed;
    public boolean ePressed;
    public boolean enterPressed;
    public boolean escapePressed;

    // Dernière direction de déplacement (pour le tir directionnel)
    public int lastDirX = 1; // par défaut : tir à droite
    public int lastDirY = 0;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    { upPressed    = true; lastDirY = -1; lastDirX = 0; }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  { downPressed  = true; lastDirY =  1; lastDirX = 0; }
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  { leftPressed  = true; lastDirX = -1; lastDirY = 0; }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) { rightPressed = true; lastDirX =  1; lastDirY = 0; }
        if (code == KeyEvent.VK_SPACE)  spacePressed  = true;
        if (code == KeyEvent.VK_E)      ePressed      = true;
        if (code == KeyEvent.VK_ENTER)  enterPressed  = true;
        if (code == KeyEvent.VK_ESCAPE) escapePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    upPressed    = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed  = false;
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  leftPressed  = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_SPACE)  spacePressed  = false;
        if (code == KeyEvent.VK_E)      ePressed      = false;
        if (code == KeyEvent.VK_ENTER)  enterPressed  = false;
        if (code == KeyEvent.VK_ESCAPE) escapePressed = false;
    }
}
