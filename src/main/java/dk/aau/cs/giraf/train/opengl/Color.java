package dk.aau.cs.giraf.train.opengl;

/**
 * A simple color class that holds RGBA values.
 * This is designed for OpenGL, which uses values ranging between 0f and 1f.
 * @author Jesper Riemer Andersen
 */
public class Color {
    /* Already created color objects. */
    public final static Color White = new Color(1f, 1f, 1f, 1f);
    public final static Color Black = new Color(0f, 0f, 0f, 1f);
    public final static Color TransparentBlack = new Color(0f, 0f, 0f, 0.15f);
    public final static Color Red = new Color(1f, 0f, 0f, 1f);
    public final static Color Green = new Color(0f, 1f, 0f, 1f);
    public final static Color Blue = new Color(0f, 0f, 1f, 1f);
    public final static Color Window = new Color(0.5f, 0.5f, 0.5f, 0.5f); // (0.6941f, 0.6902f, 0.8431f, 0.65f);
    public final static Color DarkWeather = new Color(0f, 0f, 0.2f, 0.1f);
    public final static Color BackgroundTopColor = new Color(130, 147, 255, 255);
    public final static Color BackgroundBottomColor = new Color(1f, 1f, 1f, 1f);
    public final static Color PausedOverlay = new Color(0f, 0f, 0f, 0.5f);
    public final static Color DepotBackside = new Color(53, 58, 50, 255);
    public final static Color EndOfTrack = new Color(0.8f, 0.8f, 0.8f, 1f);
    public final static Color Gray = new Color(128, 128, 128, 255);
    
    /* The color values. */
    public float red;
    public float green;
    public float blue;
    public float alpha;
    
    /** Sets color to white, without transparency */
    public Color() {
        this.setColor(1f, 1f, 1f, 1f);
    }
    
    /**
     * RGBA values ranging between 0f and 1f.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public Color(float red, float green, float blue, float alpha) {
        this.setColor(red, green, blue, alpha);
    }
    
    /**
     * RGBA values ranging between 0 and 255. The RGBA value is converted to a value between 0f and 1f.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public Color(int red, int green, int blue, int alpha) {
        this.setColor( red   / 255f,
                       green / 255f,
                       blue  / 255f,
                       alpha / 255f );
    }
    
    /**
     * Set the color. Values are between 0f and 1f.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public void setColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
    
    /**
     * Get the integer representation of the red component.
     * @return The red integer component.
     */
    public int getRedComponent() {
        return (int) this.red * 255;
    }
    
    /**
     * Get the integer representation of the green component.
     * @return The green integer component.
     */
    public int getGreenComponent() {
        return (int) this.green * 255;
    }
    
    /**
     * Get the integer representation of the blue component.
     * @return The blue integer component.
     */
    public int getBlueComponent() {
        return (int) this.blue * 255;
    }
    
    /**
     * Get the integer representation of the alpha component.
     * @return The alpha integer component.
     */
    public int getAlphaComponent() {
        return (int) this.alpha * 255;
    }
}
