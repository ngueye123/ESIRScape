package entity;

public abstract class Entity {
    public int x, y;
    public int speed;
    public int hp, maxHp;

    public abstract void update();
    public abstract void draw(java.awt.Graphics2D g2);
}
