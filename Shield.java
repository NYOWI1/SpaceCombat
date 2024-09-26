package main;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Shield {
    BufferedImage image;
    File shieldImageFile = new File("Resources/bullet2.png"); // Add a new image for the shield
    int x;
    int y;
    boolean isAlive = true;
    AffineTransform at = new AffineTransform();
    
    public Shield() {
        try {
            image = ImageIO.read(shieldImageFile);
        } catch (Exception e) {
        }
        if (image == null)
            image.createGraphics();
        setLocation();
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, image.getWidth() - 5, image.getHeight() - 5);
    }
    
    public void draw(Graphics2D g2d) {
        at.setToIdentity();
        at.translate(x, y);
        g2d.drawImage(image, at, null);
    }
    
    public void setLocation() {
        int i, j;
        i = (int)(Math.random()*2 +1);
        j = (int)(Math.random()*2 +1);
        if(i == 1) x = (int)(Math.random()*220 + 1);
        else x = (int)(Math.random()*220 + 811);
        if(j == 1) y = (int)(Math.random()*115 + 1);
        else y = (int)(Math.random()*115 + 496);
    }
}
