package assign10;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class Debris extends Participant
{

    // The outline of the bullet
    private Shape outline;
    
    public Debris (double x, double y)
    {
       Path2D rect = new Path2D.Double();
       rect.append(new Rectangle((int) x, (int) y, 1, 2), true);
       outline = rect;
    }
    
    
    @Override
    Shape getOutline ()
    {
        return outline;
    }

}
