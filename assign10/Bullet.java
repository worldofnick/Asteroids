package assign10;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class Bullet extends Participant
{

    // The outline of the bullet
    private Shape outline;


    public Bullet(double x, double y, double heading) 
    {    
        Path2D circle = new Path2D.Double();
        circle.append(new Ellipse2D.Double(x, y, 1.5, 1.5), true);
        outline = circle;
        setVelocity(Constants.BULLET_SPEED, heading);
    }
    
    @Override
    Shape getOutline ()
    {
        return outline;
    }


}
