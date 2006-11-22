package model;

import game.DeathCause;
import game.StatLogger;

import java.io.IOException;
import java.util.Random;

import com.jme.intersection.BoundingCollisionResults;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;

public class PeopleController extends Controller
{
    private static final long serialVersionUID = 1L;
    
    public static float COMEBACK_COUNTDOWN_MAX = (float)Math.PI/2;
    public static float MAKELOVE_COUNTDOWN_MAX = 1f;
    public static float DEAD_COUNTDOWN_MAX = 3f;
    public static float BORN_COUNTDOWN_MAX = 3f;
    public static float TIRED_COUNTDOWN_MAX = 3f;
    public static float HUNGRY_COUNTDOWN_MAX = 3f;
    
    private float comeBackCountdown = PeopleController.COMEBACK_COUNTDOWN_MAX;
    private float makeLoveCountdown = PeopleController.MAKELOVE_COUNTDOWN_MAX;
    private float deadCountDown = PeopleController.DEAD_COUNTDOWN_MAX;
    private float bornCountDown = PeopleController.BORN_COUNTDOWN_MAX;
    private float tiredCountDown = PeopleController.TIRED_COUNTDOWN_MAX;
    private float hungryCountDown = PeopleController.HUNGRY_COUNTDOWN_MAX;
    
    private enum PassiveStatus
    {
        WALKING, STOPING;
    }
    
    private enum WalkingDirection
    {
        FRONT, LEFT, RIGHT;
    }
    
    private PassiveStatus passiveStatus = PassiveStatus.WALKING;

    private Quaternion rot;
    
    private float actualSpeed = 0.0f;
    
    private float turningCountdown = 0.0f;   
    
    private WalkingDirection direction = WalkingDirection.FRONT;

    private Vector3f axis;

    private final People people;
    
    private float realLife;
    
    private BoundingCollisionResults collisionResults;
    
    private Random random = new Random(); 

    public PeopleController(final People people)
    {
        this.people = people;
        realLife = people.getLife();
        rot = new Quaternion();  
        axis = new Vector3f(0, 1, 0).normalizeLocal();
        collisionResults = new BoundingCollisionResults();
    }

    private float lifeTime = 0;    
    
    public void update(float time)
    {   
        this.realLife -= time;
        if(realLife <= 0f)
        {
            if(this.people.getFeeling() != PeopleFeeling.DEAD)
            {
                if(this.people.getFeeling() == PeopleFeeling.DEFENCE)
                    StatLogger.GetInstance().LogDeath(DeathCause.eated);
                else if(this.people.getFeeling() == PeopleFeeling.LOVE)
                    StatLogger.GetInstance().LogDeath(DeathCause.sex);
                else
                    StatLogger.GetInstance().LogDeath(DeathCause.natural);
            }
            this.people.setFeeling(PeopleFeeling.DEAD);            
        }
        

        if(this.people.getFeeling() == PeopleFeeling.TIRED)
        {
            this.tiredCountDown -= time;
            if(this.tiredCountDown <= 0)
            {
                this.tiredCountDown = PeopleController.TIRED_COUNTDOWN_MAX;
                this.people.setFeeling(PeopleFeeling.PASSIVE);
            }
        }
        
        if(this.people.getFeeling() == PeopleFeeling.DEFENCE)
        {
            // perd beaucoup de vie à se défendre si mauvaise défence
            this.realLife -= time*(1-this.people.getDefence()/100f)*100;
            //System.out.println("aie!");
        }

        if(this.people.getFeeling() == PeopleFeeling.HUNGRY)
        {
            this.hungryCountDown -= time;
            // si la distance le permet, mange l'autre :)
            Vector3f length = new Vector3f(this.people.getLovedPeople().getLocalTranslation());
            length.subtract(this.people.getLocalTranslation());
            if(length.length() <= 50)
            {
                // s'il s'est épuisé à se défendre
                if(this.people.getLovedPeople().getFeeling() == PeopleFeeling.DEAD)
                {                    
                    this.realLife += this.people.getLovedPeople().getLife()/2;
                    this.hungryCountDown = 0f;
                }
                else
                    this.people.getLovedPeople().setFeeling(PeopleFeeling.DEFENCE);
            }
            else
            {
                // si la personne était en train de se défendre, plus besoin
                if(this.people.getLovedPeople().getFeeling() == PeopleFeeling.DEFENCE)
                { 
                    this.people.getLovedPeople().setFeeling(PeopleFeeling.TIRED);
                } 
            }
                            
            if(this.hungryCountDown <= 0f)
            {
                this.hungryCountDown = PeopleController.HUNGRY_COUNTDOWN_MAX;
                this.people.setLovedPeople(null);
                this.people.setFeeling(PeopleFeeling.TIRED);
            }
        }
        
        people.input.update(time);
        
        updateCollision(time);
        
        updateLove(time);
        
        updateMove(time);

    }

    private void updateCollision(float time)
    {
        // s'il y a une collision avec qq chose...
        this.collisionResults.clear();
        this.people.calculateCollisions(this.people.getParent().getParent(), collisionResults);
        int collisionNumber = collisionResults.getNumber();
        if (collisionNumber > 0) 
        {                   
            // on cherche si un objet en collision correspond à un individu
            int cpt = 0;
            while(this.people.getFeeling() == PeopleFeeling.PASSIVE && cpt < collisionNumber)
            {
                Node source = this.collisionResults.getCollisionData(cpt)
                    .getSourceMesh().getParent().getParent();
                Node target = this.collisionResults.getCollisionData(cpt)
                    .getTargetMesh().getParent().getParent();
                
                if(source != target)
                {
                    // si rencontre un mur
                    /*if(!(source instanceof People) && (target instanceof People) ||
                       (source instanceof People) && !(target instanceof People))
                    {
                        this.turningCountdown = 0f;
                        this.comeBackCountdown = PeopleController.COMEBACK_COUNTDOWN_MAX;
                        this.people.setFeeling(PeopleFeeling.COMEBACK);
                    }                
                    // si on rencontre un autre, on peut l'ignorer ou procréer                
                    else*/ 
                    if(source == this.people && target instanceof People ||
                            target == this.people && source instanceof People)
                    {
                        People other = null;
                        if(source == this.people)
                            other = (People)target;
                        else
                            other = (People)source;
                        
                        Random random = new Random();
                        
                        if(other.getFeeling() == PeopleFeeling.PASSIVE)
                        {
                            // avant de faire l'amour, on verifit si l'individu a envie de manger :)
                            boolean peopleWantToEat = random.nextFloat() <= 
                                this.people.getCanibal()/100 - this.realLife/200; 
                            
                            // on calcul la probabilité qu'ils s'aiment :)                            
                            boolean peopleWantToMakeLove = random.nextFloat()*2 <=
                                (this.people.getProlific() + other.getCharming())/100;
                            boolean otherWantToMakeLove =  random.nextFloat()*2 <=
                                (other.getProlific() + this.people.getCharming())/100;
                                     
                            // s'il veut manger
                            if(peopleWantToEat)
                            {
                                this.people.setFeeling(PeopleFeeling.HUNGRY);
                                this.people.setLovedPeople(other);
                            }
                            // s'ils veulent tout les 2, alors ils copulent :)                            
                            else if(peopleWantToMakeLove && otherWantToMakeLove)
                            {
                                this.people.setFeeling(PeopleFeeling.LOVE);
                                other.setFeeling(PeopleFeeling.LOVE);
                                this.people.setLovedPeople(other);
                                other.setLovedPeople(this.people);
                            }    
                        }
                    }
                }
                
                cpt++;
            }            
        }
    }

    private void updateLove(float time)
    {
        if(this.people.getFeeling() == PeopleFeeling.LOVE)
        {
            makeLoveCountdown -= time;
            // il perd 2 fois plus de vie car faire l'amour c'est fatiguant
            this.lifeTime += time;
            if(makeLoveCountdown <= 0)
            {
                makeLoveCountdown = PeopleController.MAKELOVE_COUNTDOWN_MAX;
                this.people.setFeeling(PeopleFeeling.TIRED);
                rot.fromAngleNormalAxis((float)Math.PI, axis);
                this.people.getLocalRotation().multLocal(rot);
                // si l'autre personne a déjà fini (ou est parti avec un autre)
                // on ne créer pas de rejeton :) sinon oui...
                if(this.people.getLovedPeople() != null &&
                   this.people.getLovedPeople().getFeeling() == PeopleFeeling.LOVE &&
                   this.people.getLovedPeople().getLovedPeople() == this.people)
                {
                    try
                    {
                        createChild(time);
                        
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                this.people.setLovedPeople(null);
            }
        }
    }

    private float calculateGene(float value1, float value2)
    {
        float strong = value1>value2?value1:value2;
        float weak = value1<value2?value1:value2;
        float result = 0;
        
        // 50% de chance de prendre le fort
        // 30% de prendre le faible
        // 20% de prendre entre les deux
        Random random = new Random();
        float randomResult = random.nextFloat();
        if(randomResult <= 0.7)
            result = strong;
        else if(randomResult <= 0.9)
            result = (strong + weak) / 2f;
        else
            result = weak;
        
        // détermine la possibilité de muter
        if(random.nextFloat() <= 0.25f)
        {
            // ça peut muter en - ou en + avec 3 de max
            randomResult = random.nextFloat();
            if(randomResult <= 3f/10f)
                result += 3;
            else if(randomResult <= 5f/10f)
                result += 2;
            else if(randomResult <= 7f/10f)
                result += 1;
            else if(randomResult <= 8f/10f)
                result -= 1;
            else if(randomResult <= 9f/10f)
                result -= 2;
            else
                result -= 3;
        }
        
        return result;
            
    }
    
    private void createChild(float time) throws IOException
    {
        People child = new People(
                calculateGene(this.people.getLife(), this.people.getLovedPeople().getLife()),
                calculateGene(this.people.getCurious(), this.people.getLovedPeople().getCurious()),
                calculateGene(this.people.getSpeed(), this.people.getLovedPeople().getSpeed()),
                calculateGene(this.people.getSensibility(), this.people.getLovedPeople().getSensibility()),
                calculateGene(this.people.getFear(), this.people.getLovedPeople().getFear()),
                calculateGene(this.people.getProlific(), this.people.getLovedPeople().getProlific()),
                calculateGene(this.people.getCharming(), this.people.getLovedPeople().getCharming()),
                calculateGene(this.people.getDefence(), this.people.getLovedPeople().getDefence()),
                calculateGene(this.people.getCanibal(), this.people.getLovedPeople().getCanibal()));        
        
        // détermine la position du nouveau
        rot.fromAngleNormalAxis((float)Math.PI/2f, axis);
        rot = new Quaternion(people.getLocalRotation()).multLocal(rot);                        
        float angleZ = rot.toAngles(null)[2];
        float angleX = rot.toAngles(null)[0];
        float sinZ = (float)Math.sin(angleZ);
        float cosZ = (float)Math.cos(angleZ); 
        child.getLocalTranslation().x = sinZ*10 + this.people.getLocalTranslation().x;
        // monte ou déscend
        if(angleX < 0)
            child.getLocalTranslation().y = this.people.getLocalTranslation().y + cosZ*10;
        else if(angleX > 0)
            child.getLocalTranslation().y = this.people.getLocalTranslation().y - cosZ*10;
                                
        child.setLocalRotation(new Quaternion(people.getLocalRotation()));
        rot.fromAngleNormalAxis((float)Math.PI/2f, axis);
        child.getLocalRotation().multLocal(rot); 
        this.people.getParent().attachChild(child);
        this.people.getParent().updateGeometricState(time, true);
        child.getParent().getParent().updateCollisionTree();
        child.updateRenderState();
        
        StatLogger.GetInstance().LogBirth();
    }

    /**
     * calcul les déplacements de l'individu
     * @param time
     */
    private void updateMove(float time)
    {
        // détermine la vitesse de l'individu
        float speed = 
            ((people.getSpeed()/2) * (this.people.getCurious()/100))/2 * time*100;
        
        // determine si l'individu fait demi-tour
        /*if(this.people.getFeeling() == PeopleFeeling.COMEBACK)
        {
            // phase de décélération et demi-tour
            if(comeBackCountdown > 0)
            {
                this.passiveStatus = PassiveStatus.STOPING;
                // phase de demi-tour quand on est stopé
                if(this.actualSpeed <= 0)
                    comeBackCountdown -= time;                
            }
            // phase de "repart"
            else
            {
                this.passiveStatus = PassiveStatus.WALKING;
                // fini le demi-tour quand on a repris la vitesse normal
                if(this.actualSpeed >= speed)
                {
                    comeBackCountdown = PeopleController.COMEBACK_COUNTDOWN_MAX;
                    this.people.setFeeling(PeopleFeeling.PASSIVE);
                    turningCountdown = 0.0f;
                }
            }
        }        
        // si l'individu est amoureux (et décide de copuler) il se dirige vers la personne désirée
        else*/ if(this.people.getFeeling() == PeopleFeeling.LOVE)
        {
            this.passiveStatus = PassiveStatus.STOPING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.DEAD)
        {
            this.passiveStatus = PassiveStatus.STOPING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.BORN)
        {
            this.passiveStatus = PassiveStatus.STOPING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.PASSIVE)
        {
            this.passiveStatus = PassiveStatus.WALKING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.HUNGRY)
        {
            this.passiveStatus = PassiveStatus.WALKING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.TIRED)
        {
            this.passiveStatus = PassiveStatus.WALKING;
        }
        
        // si l'individu doit changer de direction, on descélère
        if(this.passiveStatus == PassiveStatus.STOPING)
        {
            /*if(this.actualSpeed > 0)
                this.actualSpeed -= speed/100;
            else
                this.actualSpeed = 0;*/
            this.actualSpeed = 0;
        }
        else if(this.passiveStatus == PassiveStatus.WALKING && this.actualSpeed < speed)
        {
            this.actualSpeed += speed/100f;
            if(this.actualSpeed > speed)
                this.actualSpeed = speed;
        }
        
        // détermine l'angle    
        // l'individu choisi de tourner dans une direction pour un temps donné
        // une fois ce temps écoulé, il choisi une nouvelle direction/temps
        // il y a de très forte chance qu'il ne veuille pas tourner
        /*if(this.people.getFeeling() == PeopleFeeling.COMEBACK)
        {
            if(this.actualSpeed > 0)
                this.direction = WalkingDirection.FRONT;
            else
                this.direction = WalkingDirection.LEFT;
        }
        else*/ if(this.people.getFeeling() == PeopleFeeling.PASSIVE)
        {
            this.turningCountdown -= time;
            if(this.turningCountdown <= 0)
            {   
                if(this.direction != WalkingDirection.FRONT)
                    this.direction = WalkingDirection.FRONT;
                else
                {
                    float directionRandom = this.random.nextFloat();
                    if(directionRandom >= 0.0 && directionRandom <= 0.5)
                        this.direction = WalkingDirection.FRONT;
                    else if(directionRandom < 0.75)
                        this.direction = WalkingDirection.LEFT;
                    else
                        this.direction = WalkingDirection.RIGHT;
                }
                
                this.turningCountdown = this.random.nextFloat();
                if(this.direction == WalkingDirection.FRONT)
                    this.turningCountdown -= this.people.getSpeed()/20f;
                else
                    this.turningCountdown -= this.people.getSpeed()/50f;
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.LOVE)
        {
            // se regarde les yeux dans les yeux*
            if(this.people.getLovedPeople() != null)
            {               
                Vector3f direction = new Vector3f(this.people.getLovedPeople().getLocalTranslation());
                direction.z = this.people.getLocalTranslation().z;
                
                this.people.lookAt(direction, new Vector3f(0, 0, 1));
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.HUNGRY)
        {
            // se regarde les yeux dans les yeux*
            if(this.people.getLovedPeople() != null)
            {               
                Vector3f direction = new Vector3f(this.people.getLovedPeople().getLocalTranslation());
                direction.z = this.people.getLocalTranslation().z;
                
                this.people.lookAt(direction, new Vector3f(0, 0, 1));
            }
        }
        
        
        // tourne comme il faut
        if(this.direction == WalkingDirection.RIGHT)
        {
            rot.fromAngleNormalAxis(time*(2 + this.people.getSpeed()/20f), axis);
            people.getLocalRotation().multLocal(rot);
        }
        else if((this.direction == WalkingDirection.LEFT))
        {
            rot.fromAngleNormalAxis(-time*(2 + this.people.getSpeed()/20f), axis);
            people.getLocalRotation().multLocal(rot);
        }        
                  
        // calcul la nouvelle position
        float angleZ = people.getLocalRotation().toAngles(null)[2];
        float angleX = people.getLocalRotation().toAngles(null)[0];
        float sinZ = (float)Math.sin(angleZ);
        float cosZ = (float)Math.cos(angleZ); 
        people.getLocalTranslation().x += sinZ/10 * this.actualSpeed;
        // monte ou déscend
        if(angleX < 0)
            people.getLocalTranslation().y += cosZ/10 * this.actualSpeed;
        else if(angleX > 0)
            people.getLocalTranslation().y -= cosZ/10 * this.actualSpeed;
        
        // calcul la position par rapport aux limites
        if(people.getLocalTranslation().x > 80-(people.getLife()/10))
            people.getLocalTranslation().x = -80+(people.getLife()/10);
        if(people.getLocalTranslation().x < -80+(people.getLife()/10))
            people.getLocalTranslation().x = 80-(people.getLife()/10);
        if(people.getLocalTranslation().y > 80-(people.getLife()/10))
            people.getLocalTranslation().y = -80+(people.getLife()/10);
        if(people.getLocalTranslation().y < -80+(people.getLife()/10))
            people.getLocalTranslation().y = 80-(people.getLife()/10);
        
        // calcul la dimension
        if(this.people.getFeeling() == PeopleFeeling.DEAD)
        {
            deadCountDown -= time;
            this.people.setLocalScale(deadCountDown/3);
            if(deadCountDown <= 0)
            {            
                this.people.setLocalScale(0);
                this.people.removeController(this);
                Node root = this.people.getParent();
                if(root != null)
                {
	                root.detachChild(this.people);
	                root.updateCollisionTree();
                }
                
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.BORN)
        {
            bornCountDown -= time;
            this.people.setLocalScale(1 - bornCountDown/3);
            if(bornCountDown <= 0)
            { 
                this.people.setLocalScale(1);
                this.people.setFeeling(PeopleFeeling.PASSIVE);
            }
        }
    }
    
    
}
