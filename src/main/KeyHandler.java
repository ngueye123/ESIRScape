package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Gere les appuis et relachements des touches du clavier
public class KeyHandler implements KeyListener {

    // Touches de deplacement
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Tir avec espace
    public boolean spacePressed;

    // Changement d arme avec E
    public boolean ePressed;

    // Validation menu avec ENTREE
    public boolean enterPressed;

    // Quitter avec ECHAP
    public boolean escapePressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    upPressed     = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed   = true;
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  leftPressed   = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed  = true;
        if (code == KeyEvent.VK_SPACE)                           spacePressed  = true;
        if (code == KeyEvent.VK_E)                               ePressed      = true;
        if (code == KeyEvent.VK_ENTER)                           enterPressed  = true;
        if (code == KeyEvent.VK_ESCAPE)                          escapePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    upPressed     = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed   = false;
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  leftPressed   = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed  = false;
        if (code == KeyEvent.VK_SPACE)                           spacePressed  = false;
        if (code == KeyEvent.VK_E)                               ePressed      = false;
        if (code == KeyEvent.VK_ENTER)                           enterPressed  = false;
        if (code == KeyEvent.VK_ESCAPE)                          escapePressed = false;
    }
}
