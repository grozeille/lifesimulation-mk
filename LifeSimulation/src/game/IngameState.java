package game;

import java.io.IOException;
import java.util.Random;

import model.People;
import model.PeopleFeeling;

import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Text;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.load.TransitionGameState;

public class IngameState extends CameraGameState
{
    private TransitionGameState transitionState;
    
    private Boolean gameOver = false;
    public Boolean isGameOver()
    {
        return gameOver;
    }
    
    private boolean loaded = false;
    
    private int nbPeople = 1;
    
    private CameraNode cameraNode;

    private InputHandler input;    
    
    private Skybox skyBox;
    
	private Text playerLifeText;
    private Text helpText1;
    private Text helpText2;
    private Text helpText3;
    private Text fpsText;
    
    private Node peopleNode;
        
    @Override
    public void cleanup()
    {        
        super.cleanup();
    }
    
    public IngameState(String name, int nbPeople, TransitionGameState transitionState)
    {        
        super(name);
        this.nbPeople = nbPeople;
                
        // créer la caméra        
        this.getCamera().setLocation(new Vector3f(0,0,0));
        this.getCamera().setDirection(new Vector3f(0,1,0));
        this.getCamera().setLeft(new Vector3f(1,0,0));
        this.getCamera().setFrustumFar(2000);
        
        this.transitionState = transitionState;        
        buildScene();
        handleActions();
        loaded = true;
        this.rootNode.updateRenderState();
        this.rootNode.updateGeometricState(0, true);
    }
    
    private float progress = 0;
    private String progressMessage = ""; 
    private LightState ls;
    
    private void buildScene()
    {        
        float total = nbPeople+6f;
        this.progress = 1f/total;
        this.progressMessage = "Loading mouse";
        
        // ce qui permet d'associer des actions du clavier à une action dans le jeux
        input = new InputHandler(); 
        
        mouseNode = new AbsoluteMouse("mouse", 
		                DisplaySystem.getDisplaySystem().getRenderer().getWidth(),
		                DisplaySystem.getDisplaySystem().getRenderer().getHeight());
		mouseNode.registerWithInputHandler(input);
        rootNode.attachChild(mouseNode);
        Texture textureMouse = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/arrow_s.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        AlphaState as =  DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
        as.setBlendEnabled(true);
        ts.setTexture(textureMouse);
        mouseNode.setRenderState(ts);
        mouseNode.setRenderState(as);
        mouseNode.setLightCombineMode(LightState.OFF);     
        mouseNode.updateRenderState();        
        
        peopleNode = new Node("all people");        

        try
        {
            //people1 = new People();
                        
            Random random = new Random();
            for(int cpt = 0; cpt < nbPeople; cpt++)
            {      
                this.progress = ++this.progress/total;
                this.progressMessage  = "Loading people";
                
                float angleZ = random.nextFloat() * (float)(Math.PI*4);
                float angleX = random.nextFloat() * (float)(Math.PI*4);
                float angleY = random.nextFloat() * (float)(Math.PI*4);
                
                People people = new People(
                        30,
                        30,
                        30,
                        30,
                        30,
                        30,
                        30,
                        30,
                        30);
                people.setLocalScale(0);
                people.setFeeling(PeopleFeeling.BORN);                                

                people.getLocalTranslation().x = 0f;
                people.getLocalTranslation().y = 200f;
                people.getLocalTranslation().z = 0f;
                // cherche la position initial en tournant autour du centre de la terre de X et Z
                // et cherche aussi la direction initial autour de Y
                Quaternion q = new Quaternion();
                q.fromAngles(angleX, 0, angleZ);                
                q.multLocal(people.getLocalTranslation());
                people.getLocalRotation().multLocal(q);           
                q.fromAngleNormalAxis(angleY, new Vector3f(0,1,0));
                people.getLocalRotation().multLocal(q);
                
                peopleNode.attachChild(people); 
                
                people.updateCollisionTree();
            }
            
            //rootNode.attachChild(people1);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.progress = ++this.progress/total;
        this.progressMessage  = "Loading light";
        
        // crée le "soleil", cad la lumière principale
        PointLight sun = new PointLight();
        sun.getLocation().set(0.0f, 500.0f, 0.0f);
        sun.setEnabled(true);
        sun.setAttenuate(true);
        sun.setConstant(2.0f);
        sun.setShadowCaster(true);     

        ls = DisplaySystem.getDisplaySystem().getRenderer()
                .createLightState();
        ls.setTwoSidedLighting(true);
        rootNode.setRenderState(ls);
        
        // crée le noeud caméra et lumière
        cameraNode = new CameraNode("mainCamera", this.getCamera());
        LightNode lightNode = new LightNode("sun", ls);
        lightNode.setLight(sun);
        //attache la lumière à la caméra et la caméra à la scène
        cameraNode.attachChild(lightNode);        
        rootNode.attachChild(cameraNode);        
        
        // déplace la caméra
        cameraNode.getLocalTranslation().set(0.0f, 500.0f, 0.0f);
        cameraNode.updateWorldVectors();
        cameraNode.lookAt(new Vector3f(0.0f, 0.01f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        cameraNode.updateWorldVectors();
                
        
        ray = new Ray(this.getCamera().getLocation(), this.getCamera().getDirection());
		pickResults = new BoundingPickResults();		
        
        this.progress = ++this.progress/total;
        this.progressMessage  = "Loading HUD";
        
        // petit "HUD"
        playerLifeText = Text.createDefaultTextLabel( "camera info", "" );          
        playerLifeText.getLocalTranslation().set(
                0,DisplaySystem.getDisplaySystem().getHeight()-20,0);
        playerLifeText.setLightCombineMode(LightState.OFF);        
        rootNode.attachChild(playerLifeText);	
        fpsText = Text.createDefaultTextLabel( "fpsText", "" );          
        fpsText.getLocalTranslation().set(
                0,100,0);
        fpsText.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(fpsText);    
        helpText1 = Text.createDefaultTextLabel( "helpText1", "" );          
        helpText1.getLocalTranslation().set(
                0,80,0);
        helpText1.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText1);    
        helpText2 = Text.createDefaultTextLabel( "helpText2", "" );          
        helpText2.getLocalTranslation().set(
                0,40,0);
        helpText2.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText2);
        helpText3 = Text.createDefaultTextLabel( "helpText3", "" );          
        helpText3.getLocalTranslation().set(
                0,20,0);
        helpText3.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText3);
        
        this.progress = ++this.progress/total;
        this.progressMessage  = "Loading skybox";
        
        // un fond 
        buildSkyBox();
        
        this.progress = ++this.progress/total;
        this.progressMessage  = "Loading planet";

        // et la planet
        buildPlanet();
        
        rootNode.attachChild(peopleNode);
        peopleNode.setRenderState(ls);
        
        // maj de la scène        
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();
    }

    //private final float[] lightPosition = { 0.8f, 0.8f, 0.8f, 0.0f };
    
    /**
     * construit les murs invisibles pour la gestion des collisions sur les bords
     * @param height
     * @param length
     * @param width
     */
	private void buildPlanet()
	{
		Texture textureGrass = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/planetautre.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setTexture(textureGrass);
        ts.setEnabled(true);
        
        MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
        //ms.getEmissive().g = 0.001f;
        ms.getDiffuse().r = 0.2f;
        ms.getDiffuse().g = 1f;
        ms.getDiffuse().b = 0.2f;
        //ms.getAmbient().g = 0.001f;   
        ms.setEnabled(true);

        
        // contruit la planète
        planet = new Node("planet");
        rootNode.attachChild(planet);
        Sphere planetSphere = new Sphere("planetSphere", 18, 18, 195);
        planetSphere.setRenderState(ts);
        planetSphere.setRenderState(ls);
        //planetSphere.setRenderState(ms);        
        planet.attachChild(planetSphere);
                
        /*Quaternion q = new Quaternion();
        q.fromAngleAxis((float)Math.PI/2f, new Vector3f(1,0,0));        
        planet.getLocalRotation().multLocal(q);*/
        rootNode.updateRenderState();
        
	}

    private void buildSkyBox() 
    {
        this.skyBox = new Skybox("skybox", 600, 600, 600);
        //Quaternion rot = new Quaternion();
        //Vector3f axis = new Vector3f(1, 0, 0).normalizeLocal();
        //rot.fromAngleNormalAxis((float)Math.PI/4, axis);
        //skyBox.getLocalRotation().multLocal(rot);
        
        Texture all = TextureManager.loadTexture(
                getClass().getResource(
            "/ressources/nuage.png"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);        
 
        skyBox.setTexture(Skybox.NORTH, all);
        skyBox.setTexture(Skybox.WEST, all);
        skyBox.setTexture(Skybox.SOUTH, all);
        skyBox.setTexture(Skybox.EAST, all);
        skyBox.setTexture(Skybox.UP, all);
        skyBox.setTexture(Skybox.DOWN, all);
        //skyBox.preloadTextures();
        rootNode.attachChild(skyBox);
    }
    
	/**
	 * associe les actions clavier avec des actions dans le jeux
	 *
	 */
	private void handleActions()
	{       
	    input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                gameOver = true;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE,
                InputHandler.AXIS_NONE, true);
        
        /////////////////// camera
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                Quaternion q = new Quaternion();
                Vector3f y = new Vector3f(0,1,0);
                y = cameraNode.getLocalRotation().mult(y);
                q.fromAngleAxis(-evt.getTime(), y);
                Vector3f v = new Vector3f(cameraNode.getLocalTranslation());
                v = q.mult(v);
                Vector3f up = new Vector3f(0,1,0);
                up = cameraNode.getLocalRotation().mult(up);
                cameraNode.setLocalTranslation(v);                
                cameraNode.lookAt(new Vector3f(0,0,0), up);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LEFT,
                InputHandler.AXIS_NONE, true);        
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                Quaternion q = new Quaternion();
                Vector3f y = new Vector3f(0,1,0);
                y = cameraNode.getLocalRotation().mult(y);
                q.fromAngleAxis(evt.getTime(), y);
                Vector3f v = new Vector3f(cameraNode.getLocalTranslation());
                v = q.mult(v);
                Vector3f up = new Vector3f(0,1,0);
                up = cameraNode.getLocalRotation().mult(up);
                cameraNode.setLocalTranslation(v);                
                cameraNode.lookAt(new Vector3f(0,0,0), up);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RIGHT,
                InputHandler.AXIS_NONE, true);
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                Quaternion q = new Quaternion();
                Vector3f x = new Vector3f(1,0,0);
                x = cameraNode.getLocalRotation().mult(x);
                q.fromAngleAxis(evt.getTime(), x);
                Vector3f v = new Vector3f(cameraNode.getLocalTranslation());
                v = q.mult(v);
                Vector3f up = new Vector3f(0,1,0);
                up = cameraNode.getLocalRotation().mult(up);
                cameraNode.setLocalTranslation(v);                
                cameraNode.lookAt(new Vector3f(0,0,0), up);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_UP,
                InputHandler.AXIS_NONE, true);
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                Quaternion q = new Quaternion();
                Vector3f x = new Vector3f(1,0,0);
                x = cameraNode.getLocalRotation().mult(x);
                q.fromAngleAxis(-evt.getTime(), x);
                Vector3f v = new Vector3f(cameraNode.getLocalTranslation());
                v = q.mult(v);
                Vector3f up = new Vector3f(0,1,0);
                up = cameraNode.getLocalRotation().mult(up);
                cameraNode.setLocalTranslation(v);                
                cameraNode.lookAt(new Vector3f(0,0,0), up);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_DOWN,
                InputHandler.AXIS_NONE, true);
        
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                // calcul de combien diviser le vecteur pour obtenir un pas de
                // 1 * evt.getTime()
                float length = cameraNode.getLocalTranslation().length();
                float factor = (length - evt.getTime()*100)/length;
                cameraNode.getLocalTranslation().multLocal(factor);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ADD,
                InputHandler.AXIS_NONE, true);
        
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                // calcul de combien diviser le vecteur pour obtenir un pas de
                // 1 * evt.getTime()
                float length = cameraNode.getLocalTranslation().length();
                float factor = (length - evt.getTime()*100)/length;
                cameraNode.getLocalTranslation().divideLocal(factor);
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_SUBTRACT,
                InputHandler.AXIS_NONE, true);
        
        
        //input.addAction(new MousePick(getCamera(), rootNode, helpText3));
        input.addAction(new MouseInputAction()
        {
            public void performAction(InputActionEvent evt)
            {
            	 if( MouseInput.get().isButtonDown(0))
            	 {
                     //System.out.println("mouse:"+mouseNode.getLocalTranslation());
                     /*Vector3f x = new Vector3f(mouseNode.getLocalTranslation().x-DisplaySystem.getDisplaySystem().getRenderer().getWidth()/2f,0,0);
                     Vector3f y = new Vector3f(0,mouseNode.getLocalTranslation().y-DisplaySystem.getDisplaySystem().getRenderer().getHeight()/2f,0);
                     x = cameraNode.getLocalRotation().mult(x);
                     y = cameraNode.getLocalRotation().mult(y);*/
                     /*Vector3f location = new Vector3f(cameraNode.getLocalTranslation());
                     location.z += mouseNode.getLocalTranslation().y;
                     location.x += mouseNode.getLocalTranslation().x;
                     ray = new Ray(location, getCamera().getDirection());*/
                     /*ray = new Ray(
                             cameraNode.getLocalTranslation().add(x).add(y),
                             cameraNode.getLocalTranslation().negate()); // camera direction is already normalized*/                     
                     //ray = new Ray(getCamera().getWorldCoordinates(new Vector2f, zPos), getCamera().getDirection())
                     /*System.out.println("world"+getCamera().getWorldCoordinates(
                             new Vector2f(mouseNode.getLocalTranslation().x,
                                          mouseNode.getLocalTranslation().y), cameraNode.getLocalTranslation().z));*/
                     pickResults.setCheckDistance(true);
                     rootNode.findPick(ray,pickResults);
            	 }
            }

        });

	}
    
    private float elapsedTime = 0;
    private float elapsedFrame = 0;    
    private float logTime = 0;

    private Node planet;

    private float stateTime = 0;
    //private int numberPeople = 0;

	private AbsoluteMouse mouseNode;

	private Ray ray;

	private PickResults pickResults;
    
    private float elapsedVirusTime = 0;

    static private float fps = 100;
    static public float getFps()
    {
        return fps;
    }
    
    private People selected = null;
       
    @Override
    protected void stateUpdate(float tpf)
    {
        if(!loaded)
        {
            transitionState.setProgress(this.progress, this.progressMessage);                      
            return;
        }
        transitionState.setActive(false);
        
        input.update(tpf); 
        stateTime += tpf;
        elapsedTime += tpf;
        elapsedVirusTime += tpf;
        logTime += tpf;
        
        if(logTime >= 10.0f)
        {
            StatLogger.GetInstance().LogPeople(peopleNode.getChildren());
            logTime = 0;
        }
        
        if(stateTime < 0.5f)
            return;        
        
        IngameState.fps = ((Float)(elapsedFrame/stateTime)).intValue();
        if(elapsedTime < 6)
            IngameState.fps = 100;
        elapsedFrame = 0.0f;
        stateTime = 0.0f;
                
                
        if(pickResults.getNumber() > 0)
        {
            if(this.selected != null)
                this.selected.setSelected(false);
            
        	Node source = pickResults.getPickData(0)
                    .getTargetMesh().getParentGeom().getParent().getParent();
        	if(source instanceof People)
        	{
                this.selected = (People)source;     
                this.selected.setSelected(true);
                System.out.println("caca");
        	}
        }
        pickResults.clear();
        
        // maladie HAHAHA!
        /*if(fps < 20f && elapsedVirusTime > 2f)
        {
            int toBeKilled = 1;
            Random random = new Random();
            
            if(fps < 7 && peopleNode.getChildren().size() > 80)
            {
                toBeKilled = 60;
                for(int cpt = 0; cpt < toBeKilled; cpt++)
                {
                    ((People)peopleNode.getChild(random.nextInt(nbPeople-1))).setFeeling(PeopleFeeling.DEAD);
                    StatLogger.GetInstance().LogDeath(DeathCause.disease);
                } 
            }
            else
            {
                if(peopleNode.getChildren().size() > 80)
                    toBeKilled = 5;
                else if(peopleNode.getChildren().size() > 90)
                    toBeKilled = 10;
                else if(peopleNode.getChildren().size() > 100)
                    toBeKilled = 30;
                else if(peopleNode.getChildren().size() > 110)
                    toBeKilled = 60;            
                else if(peopleNode.getChildren().size() > 120)
                    toBeKilled = 100;
                else if(peopleNode.getChildren().size() > 150)
                    toBeKilled = 130;
                
                for(int cpt = 0; cpt < toBeKilled; cpt++)
                {
                    ((People)peopleNode.getChild(random.nextInt(nbPeople-1))).setFeeling(PeopleFeeling.SICK);                
                }
            }
            elapsedVirusTime = 0;
        }*/        
        
        /*for(Spatial s : peopleNode.getChildren())
        {     
            if(s instanceof People)
            {
                People p = (People)s;
                if(p.getLocalTranslation().x > 100 || 
                   p.getLocalTranslation().x < -100 ||
                   p.getLocalTranslation().y > 100 ||
                   p.getLocalTranslation().y < -100)
                {
                    p.setFeeling(PeopleFeeling.DEAD);
                }
            }
        }*/
        helpText1.print("nb individu : "+peopleNode.getQuantity());
        helpText2.print("elapsed time : "+(int)elapsedTime/60+"min "+(int)elapsedTime%60+"sec");
        helpText3.print("[UP][DOWN][LEFT][RIGHT] pour déplacer la caméra, [NUMPAD_+][NUMPAD_-] pour zoomer");
        fpsText.print("fps : "+fps);                     
        
        super.stateUpdate(tpf);
    }
    
    
    @Override
    protected void onActivate()
    {
        super.onActivate();
    }
    
    @Override
    protected void onDeactivate()
    {
        input.clearActions();
        super.onDeactivate();
    }
        
    
    @Override
    protected void stateRender(float tpf)
    {            	
        elapsedFrame++;
        DisplaySystem.getDisplaySystem().getRenderer().clearBuffers();
        super.stateRender(tpf);        
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public float getProgress()
    {
        return progress;
    }

    public String getProgressMessage()
    {
        return progressMessage;
    }

}
