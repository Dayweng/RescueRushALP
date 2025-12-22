import java.awt.*;

public class testDimensions {
    static int Screenwidth;
    static int Screenheight;
    
    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Screenwidth = (int) screenSize.getWidth();
            Screenheight = (int) screenSize.getHeight();
        
        System.out.println(Screenheight);
        System.out.println(Screenwidth);
    }
}
