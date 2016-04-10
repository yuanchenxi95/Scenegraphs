package util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 A class that represents an image. Provides a function for bilinear interpolation
 */
public class TextureImage
{
    private BufferedImage image;
    private String name;
    private String imageFormat;
    private Texture texture;
    private InputStream in;
    private String path;

    public TextureImage(String path, String name, String imageFormat) throws IOException
    {
        //read the image
        this.path = path;
        in = getClass().getClassLoader().getResourceAsStream(path);
        image = ImageIO.read(in);
        this.name = new String(name);
        this.imageFormat = new String(imageFormat);
    }

    public String getName()
    {
        return name;
    }

    public Texture getTexture() {
        if (this.texture == null) {
                   try {
                       in = getClass().getClassLoader().getResourceAsStream(path);
                       this.texture = TextureIO.newTexture(this.in, true, imageFormat);
                   } catch (IOException io) {
                       io.printStackTrace();
                   }

        }

        return this.texture;
    }

    Vector4f getColor(float x,float y)
    {
        int x1,y1,x2,y2;

        x = x - (int)x; //GL_REPEAT
        y = y - (int)y; //GL_REPEAT

        x1 = (int)(x*image.getWidth());
        y1 = (int)(y*image.getHeight());

        x1 = (x1 + image.getWidth())%image.getWidth();
        y1 = (y1 + image.getHeight())%image.getHeight();

        x2 = x1+1;
        y2 = y1+1;

        if (x2>=image.getWidth())
            x2 = image.getWidth()-1;

        if (y2>=image.getHeight())
            y2 = image.getHeight()-1;

        Vector4f one = ColorToVector4f(new Color(image.getRGB(x1,y1)));
        Vector4f two = ColorToVector4f(new Color(image.getRGB(x2,y1)));
        Vector4f three = ColorToVector4f(new Color(image.getRGB(x1,y2)));
        Vector4f four = ColorToVector4f(new Color(image.getRGB(x2,y2)));

        Vector4f inter1 = one.lerp(three,y-y1);
        Vector4f inter2 = two.lerp(four,y-y1);
        Vector4f inter3 = inter1.lerp(inter2,x-x1);

        return inter3;
    }

    private Vector4f ColorToVector4f(Color c)
    {
        return new Vector4f((float)c.getRed()/255,(float)c.getGreen()/255,(float)c.getBlue()/255,(float)c.getAlpha()/255);
    }


}
