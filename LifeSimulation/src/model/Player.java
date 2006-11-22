package model;

import java.io.IOException;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
//import com.jmex.physics.impl.ode.DynamicPhysicsNodeImpl;
//import com.jmex.physics.impl.ode.OdePhysicsSpace;
public class Player
{}
/*public class Player extends DynamicPhysicsNodeImpl
{
    private static final long serialVersionUID = 8648663467990512362L;

    private Node model;
    
    public Player(OdePhysicsSpace space) throws IOException
    {
        super(space);
        this.model = ModelManager.getInstance().loadModel("player");
        this.model.setLocalTranslation(this.getLocalTranslation());            
        this.model.setLocalScale(0.04f);
        this.attachChild(this.model);
        
        BoundingSphere bounding = new BoundingSphere(100.0f, 
                new Vector3f(this.getLocalTranslation().x,
                        this.getLocalTranslation().y,
                        this.getLocalTranslation().z));

        this.setModelBound(bounding);                                    
        this.generatePhysicsGeometry();
        this.lookAt(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f));
    }    
}*/
