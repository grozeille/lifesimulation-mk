package model;

import game.DeathCause;
import game.IngameState;
import game.StatLogger;

import java.util.ArrayList;
import java.util.Random;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

public class PeopleController extends Controller
{
    private static final long serialVersionUID = 1L;
    
    public static float MAKELOVE_COUNTDOWN_MAX = 6f;
    public static float DEAD_COUNTDOWN_MAX = 3f;
    public static float BORN_COUNTDOWN_MAX = 3f;
    public static float TIRED_COUNTDOWN_MAX = 3f;
    public static float HUNGRY_COUNTDOWN_MAX = 3f;
    public static float SICKIMMUNITY_COUNTDOWN_MAX = 3f;

    private float makeLoveCountdown = PeopleController.MAKELOVE_COUNTDOWN_MAX;
    private float deadCountDown = PeopleController.DEAD_COUNTDOWN_MAX;
    private float bornCountDown = PeopleController.BORN_COUNTDOWN_MAX;
    private float tiredCountDown = PeopleController.TIRED_COUNTDOWN_MAX;
    private float hungryCountDown = PeopleController.HUNGRY_COUNTDOWN_MAX;
    private float sickImmunityCountDown = PeopleController.SICKIMMUNITY_COUNTDOWN_MAX;
    
    private enum PassiveStatus
    {
        WALKING, STOPING, RUNNING, CRAWLING;
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
    
    private PeopleFactory peopleFactory;
    
    //private BoundingCollisionResults collisionResults;
    
    private Random random = new Random(); 

    public PeopleController(final People people)
    {
        this.people = people;
        realLife = people.getChromosome()[People.GENE_LIFE]*3;
        rot = new Quaternion();  
        axis = new Vector3f(people.getLocalTranslation()).normalizeLocal();
        //collisionResults = new BoundingCollisionResults();
    }
    
    private float stateTime = 0;
    
    private float sickTime = 0;    
    
    public void update(float time)
    {   
        stateTime += time;
        if(stateTime < 0.5f)
            return;
        
        this.realLife -= time;
        
        people.input.update(time);
        
        updateSick(time);
        
        updateDead();
        
        updateTired(time);
        
        updateDefence(time);

        updateHungry(time);        
        
        updateCollision(time);
        
        updateLove(time);
        
        updateMove(time);

    }

    private void updateHungry(float time)
    {
        if(this.people.getFeeling() == PeopleFeeling.HUNGRY)
        {
            this.hungryCountDown -= time;
            // si la distance le permet, mange l'autre :)
            float length = this.people.getLovedPeople().getLocalTranslation()
                .distance(this.people.getLocalTranslation());            
            if(length <= 10)
            {
                // s'il s'est épuisé à se défendre
                if(this.people.getLovedPeople().getFeeling() == PeopleFeeling.DEAD)
                {   
                    this.realLife += this.people.getLovedPeople().getChromosome()[People.GENE_LIFE]/2;
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
    }

    private void updateDefence(float time)
    {
        if(this.people.getFeeling() == PeopleFeeling.DEFENCE)
        {
            // perd beaucoup de vie à se défendre si mauvaise défence
            this.realLife -= time*(1-this.people.getChromosome()[People.GENE_DEFENCE]/100f)*100;
        }
    }

    private void updateTired(float time)
    {
        if(this.people.getFeeling() == PeopleFeeling.TIRED)
        {
            this.tiredCountDown -= time;
            if(this.tiredCountDown <= 0)
            {
                this.tiredCountDown = PeopleController.TIRED_COUNTDOWN_MAX;
                this.people.setFeeling(PeopleFeeling.PASSIVE);
            }
        }
    }

    private void updateDead()
    {
        if(realLife <= 0f)
        {
            if(this.people.getFeeling() != PeopleFeeling.DEAD)
            {
                if(this.people.getFeeling() == PeopleFeeling.DEFENCE)
                    StatLogger.GetInstance().LogDeath(DeathCause.eated);
                else if(this.people.getFeeling() == PeopleFeeling.LOVE)
                    StatLogger.GetInstance().LogDeath(DeathCause.sex);
                else if(this.people.getFeeling() == PeopleFeeling.SICK)
                    StatLogger.GetInstance().LogDeath(DeathCause.disease);
                else
                    StatLogger.GetInstance().LogDeath(DeathCause.natural);
            }
            this.people.setFeeling(PeopleFeeling.DEAD);            
        }
    }

    private void updateSick(float time)
    {
        this.sickImmunityCountDown -= time;
        
        if(this.people.getFeeling() == PeopleFeeling.SICK)
        {
            if(sickImmunityCountDown > 0)
            {
                this.people.setFeeling(PeopleFeeling.TIRED);
            }
            else
            {
                sickTime += time;
                if(sickTime > 10f)
                {
                    this.people.setFeeling(PeopleFeeling.TIRED);
                    sickTime = 0;
                    sickImmunityCountDown = PeopleController.SICKIMMUNITY_COUNTDOWN_MAX;
                }
                else
                    this.realLife -= time*(100-this.people.getChromosome()[People.GENE_DEFENCE]);
            }
        }
    }

    private void updateCollision(float time)
    {
        // s'il y a une collision avec qq chose...
        //this.collisionResults.clear();
        //this.people.calculateCollisions(this.people.getParent().getParent(), collisionResults);
        //int collisionNumber = collisionResults.getNumber();
        ArrayList<People> peopleList = new ArrayList<People>();
        for(Spatial s : this.people.getParent().getChildren())
        {
            if(s != this.people)
            {
                float distance = s.getLocalTranslation().distance(
                        this.people.getLocalTranslation());
                // si la personne est dans son champs de vision
                if(distance < 0.5f*this.people.getChromosome()[People.GENE_SENSIBILITY])
                {
                    if(s instanceof People)
                        peopleList.add((People)s);                    
                }
            }
        }
        if (peopleList.size() > 0) 
        {               
            // contamine tous le monde autour
            /*if(this.people.getFeeling() == PeopleFeeling.SICK)
            {
                for(People p : peopleList)
                {
                    p.setFeeling(PeopleFeeling.SICK);
                }
            }*/
            
            // on cherche si un objet en collision correspond à un individu
            int cpt = 0;
            while(this.people.getFeeling() == PeopleFeeling.PASSIVE && cpt < peopleList.size())
            {
                /*Node source = this.collisionResults.getCollisionData(cpt)
                    .getSourceMesh().getParent().getParent();
                Node target = this.collisionResults.getCollisionData(cpt)
                    .getTargetMesh().getParent().getParent();*/
                People other = peopleList.get(cpt);
                
                Random random = new Random();

                // avant de faire l'amour, on verifit si l'individu a envie de manger :)
                boolean peopleWantToEat = random.nextFloat() <= 
                    (this.people.getChromosome()[People.GENE_CANIBAL])/100 - this.realLife/100; 
                
                // on calcul la probabilité qu'ils s'aiment :)                            
                boolean peopleWantToMakeLove = random.nextFloat()*2 <=
                    (this.people.getChromosome()[People.GENE_PROLIFIC] + other.getChromosome()[People.GENE_CHARMING])/100;
                boolean otherWantToMakeLove =  random.nextFloat()*2 <=
                    (other.getChromosome()[People.GENE_PROLIFIC] + this.people.getChromosome()[People.GENE_CHARMING])/100;
                
                // s'il veut manger
                if(peopleWantToEat)
                {
                    this.people.setFeeling(PeopleFeeling.HUNGRY);
                    this.people.setLovedPeople(other);
                }
                // s'ils veulent tout les 2, alors ils copulent :)                            
                else if(other.getFeeling() == PeopleFeeling.PASSIVE && 
                        peopleWantToMakeLove && otherWantToMakeLove)
                {
                    this.people.setFeeling(PeopleFeeling.LOVE);
                    other.setFeeling(PeopleFeeling.LOVE);
                    this.people.setLovedPeople(other);
                    other.setLovedPeople(this.people);
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
            this.realLife -= time;
            
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
                    peopleFactory = new PeopleFactory(this.people, this.people.getLovedPeople());
                    peopleFactory.start();  
                    this.people.setFeeling(PeopleFeeling.GESTATION);
                }
                this.people.setLovedPeople(null);
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.GESTATION)
        {
            People child = peopleFactory.getChild();
            // il est pret ! il peut naitre !
            if(child != null)
            {                
                child.getLocalTranslation().set(new Vector3f(this.people.getLocalTranslation()));
                child.getLocalRotation().set(new Quaternion(this.people.getLocalRotation()));
                child.setLocalScale(0);
                child.setFeeling(PeopleFeeling.BORN);
                this.people.getParent().attachChild(child);
                //this.people.getParent().updateGeometricState(time, true);
                child.updateRenderState();
                this.people.setFeeling(PeopleFeeling.TIRED);
                
                StatLogger.GetInstance().LogBirth();
            }
        }
    }

    /**
     * calcul les déplacements de l'individu
     * @param time
     */
    private void updateMove(float time)
    {
        // détermine la vitesse de l'individu
        float speed = 
            (((people.getChromosome()[People.GENE_SPEED]+10f)/2f) * 
             (this.people.getChromosome()[People.GENE_CURIOUS]/100f)
             )/2f * time*100f;
           
        // si l'individu est amoureux (et décide de copuler) il se dirige vers la personne désirée
        if(this.people.getFeeling() == PeopleFeeling.LOVE || 
           this.people.getFeeling() == PeopleFeeling.DEAD ||
           this.people.getFeeling() == PeopleFeeling.BORN)
        {
            this.passiveStatus = PassiveStatus.STOPING;
        }
        else if(this.people.getFeeling() == PeopleFeeling.PASSIVE || 
                this.people.getFeeling() == PeopleFeeling.HUNGRY ||
                this.people.getFeeling() == PeopleFeeling.GESTATION ||
                this.people.getFeeling() == PeopleFeeling.TIRED)
        {
            this.passiveStatus = PassiveStatus.WALKING;
        }        
        else if(this.people.getFeeling() == PeopleFeeling.SICK)
        {
            this.passiveStatus = PassiveStatus.CRAWLING;
        }
        
        if(this.passiveStatus == PassiveStatus.STOPING)
        {
            this.actualSpeed = speed/4;
        }
        // si l'individu doit changer de direction, on descélère
        else if(this.passiveStatus == PassiveStatus.STOPING)
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
        
        axis = new Vector3f(0,1,0);
        // détermine l'angle    
        // l'individu choisi de tourner dans une direction pour un temps donné
        // une fois ce temps écoulé, il choisi une nouvelle direction/temps
        // il y a de très forte chance qu'il ne veuille pas tourner
        if(this.people.getFeeling() == PeopleFeeling.PASSIVE || this.people.getFeeling() == PeopleFeeling.GESTATION)
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
                    this.turningCountdown -= this.people.getChromosome()[People.GENE_SPEED]/20f;
                else
                    this.turningCountdown -= this.people.getChromosome()[People.GENE_SPEED]/50f;
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.LOVE)
        {
            // se regarde les yeux dans les yeux
            if(this.people.getLovedPeople() != null)
            {               
                //Quaternion q = new Quaternion(this.people.getLocalRotation());
                this.people.lookAt(this.people.getLovedPeople().getLocalTranslation(),
                        this.people.getLocalTranslation());

                /*float newAngleZ = this.people.getLocalRotation().toAngleAxis(axis);
                float angleZ = q.toAngleAxis(axis);
                this.people.setLocalRotation(new Quaternion(q));
                q.fromAngleAxis(newAngleZ-angleZ, axis);
                this.people.getLocalRotation().multLocal(q);*/
                
            }
        }
        else if(this.people.getFeeling() == PeopleFeeling.HUNGRY)
        {
            // se troune vers l'individu pour le pourchasser
            if(this.people.getLovedPeople() != null)
            {               
                //Quaternion q = new Quaternion(this.people.getLocalRotation());
                this.people.lookAt(this.people.getLovedPeople().getLocalTranslation(),
                        this.people.getLocalTranslation());

                /*float newAngleZ = this.people.getLocalRotation().toAngleAxis(axis);
                float angleZ = q.toAngleAxis(axis);
                this.people.setLocalRotation(new Quaternion(q));
                q.fromAngleAxis(newAngleZ-angleZ, axis);
                this.people.getLocalRotation().multLocal(q);*/
            }
        }
        
        if(this.actualSpeed > 0f)
        {        
            float angleDirection = 0f; 
            
            // détermine la nouvelle direction
            if(this.direction == WalkingDirection.RIGHT)
            {
                angleDirection = time*(2f + this.people.getChromosome()[People.GENE_SPEED]/20f);

            }
            else if((this.direction == WalkingDirection.LEFT))
            {
                angleDirection = -time*(2f + this.people.getChromosome()[People.GENE_SPEED]/20f);
            }
                       
            
            rot.fromAngleNormalAxis(angleDirection, axis);
            people.getLocalRotation().multLocal(rot);
        
            Quaternion q = new Quaternion();
            Vector3f vX;
            // trouve l'axe X de la personne        
            // 1ère solution qui dépend trop de la rotation de l'individu 
            vX = new Vector3f(1,0,0);
            q = new Quaternion(this.people.getLocalRotation());
            q.normalize();
            vX = q.mult(vX);
            
            // crée une rotation autour de cet axe X
            q.fromAngleAxis(this.actualSpeed/1000f, vX);
            // applique la rotation au vecteur "rayon" de la sphère
            // et ainsi détermine la position de l'individu
            this.people.setLocalTranslation(q.mult(this.people.getLocalTranslation()));
            
            
            // détermine l'axe Z de la personne pour qu'elle regarde devant elle (même si et se dirige vers quelqu'un)
            q = new Quaternion();
            q.fromAngleAxis(-(float)Math.PI/2f, new Vector3f(this.people.getLocalTranslation()));
            Vector3f vZ = q.mult(vX);
            this.people.lookAt(vZ.add(this.people.getLocalTranslation()),
                    this.people.getLocalTranslation());
            // applique la même rotation à l'individu autour de son axe X    
            /*q.fromAngleNormalAxis(this.actualSpeed/1000f, vX.normalize());
            this.people.getLocalRotation().multLocal(q);*/
        
        }
        
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
	                // TODO ?root.updateCollisionTree();
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
                this.people.setFeeling(PeopleFeeling.TIRED);
                // s'il n'y a pas assez de FPS, il est malade :(
                if(IngameState.getFps() < 20)
                {
                    this.people.setFeeling(PeopleFeeling.SICK);
                }
            }
        }
    }
}
