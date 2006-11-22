package game;

import java.io.IOException;
import java.util.Random;

import model.People;
import model.PeopleFeeling;

import com.jme.bounding.BoundingBox;
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
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.game.state.StandardGameState;

public class IngameState extends StandardGameState
{
    private Boolean gameOver = false;
    public Boolean isGameOver()
    {
        return gameOver;
    }
        
    //private Player player;
    
    //private Float playerFireRate = 0.2f;
    
    //private float playerSpeed = 8.0f;   
    
    private CameraNode cameraNode;

    private InputHandler input;

    //private OdePhysicsSpace physicsSpace;       
    
    private Skybox skyBox;
    
	private Text playerLifeText;
    private Text helpText1;
    private Text helpText2;
    private Text helpText3;
    private Text fpsText;
    
    private Node peopleNode;
    
    private Boolean showDebug = false;
        
    @Override
    public void cleanup()
    {        
        super.cleanup();
        //physicsSpace.cleanup();
    }
    
    public IngameState(String name)
    {
        super(name);
        
        // ce qui permet d'associer des actions du clavier à une action dans le jeux
        input = new InputHandler(); 
        
        // crée le "soleil", cad la lumière principale
        PointLight sun = new PointLight();
        sun.getLocation().set(0.0f, 0.0f, 500.0f);
        sun.setEnabled(true);
        sun.setAttenuate(true);
        sun.setConstant(2.0f);
        sun.setShadowCaster(true);                
        
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
        ts.setTexture(textureMouse);
        mouseNode.setRenderState(ts);
        mouseNode.setRenderState(as);
        mouseNode.setLightCombineMode(LightState.OFF);     
        mouseNode.updateRenderState();

        // active la lumière de la scène
        LightState ls = DisplaySystem.getDisplaySystem().getRenderer()
                .createLightState();
        ls.attach(sun);
        ls.setTwoSidedLighting(true);
        rootNode.setRenderState(ls);
        
        peopleNode = new Node("all people");        
        
        // crée "l'espace" soumis aux force (� l'aide de ODE)
        //physicsSpace = new OdePhysicsSpace();
        //physicsSpace.setDirectionalGravity(new Vector3f(0.0f,0.0f,-1.0f));

        try
        {
            //people1 = new People();
                        
            Random random = new Random();
            for(int cpt = 0; cpt < 50; cpt++)
            {                
                float angleZ = random.nextFloat() * (float)(Math.PI*4);
                int posX = random.nextInt(50)-25;
                int posY = random.nextInt(50)-25;
                
                People people = new People(
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100),
                        random.nextInt(100));
                peopleNode.attachChild(people);                
                
                people.getLocalTranslation().x = posX;
                people.getLocalTranslation().y = posY;
                Quaternion rot = new Quaternion();  
                Vector3f axis = new Vector3f(0, 1, 0).normalizeLocal();
                rot.fromAngleNormalAxis(angleZ, axis);
                people.getLocalRotation().multLocal(rot);
                people.updateCollisionTree();
            }
            
            //rootNode.attachChild(people1);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                
        
        // crée le joueur
        /*player = new Player(physicsSpace, complex);
        //ennemy1 = new SimpleEnnemy(physicsSpace);
        rootNode.attachChild(player);
        physicsSpace.addNode(player);
        player.setLocalTranslation(new Vector3f(0.0f, -40.0f, 0.0f));
        */
        
        // créer la caméra        
        this.getCamera().setLocation(new Vector3f(0,0,0));
        this.getCamera().setDirection(new Vector3f(0,1,0));
        this.getCamera().setLeft(new Vector3f(1,0,0));
        cameraNode = new CameraNode("mainCamera", this.getCamera());
        rootNode.attachChild(cameraNode);
        cameraNode.getLocalTranslation().set(0.0f, -150.0f, 150.0f);
        cameraNode.updateWorldVectors();
        cameraNode.lookAt(new Vector3f(0.0f, 0.01f, -1.0f), new Vector3f(0.0f, 0.0f, 1.0f));
        cameraNode.updateWorldVectors();
        
        ray = new Ray(this.getCamera().getLocation(), this.getCamera().getDirection());
		pickResults = new BoundingPickResults();		
        
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
        helpText1 = Text.createDefaultTextLabel( "helpText1", "[UP][DOWN][LEFT][RIGHT] pour se deplacer," );          
        helpText1.getLocalTranslation().set(
                0,80,0);
        helpText1.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText1);    
        helpText2 = Text.createDefaultTextLabel( "helpText2", "[SPACE] pour tirer, [ESC] pour mettre en pause," );          
        helpText2.getLocalTranslation().set(
                0,40,0);
        helpText2.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText2);
        helpText3 = Text.createDefaultTextLabel( "helpText3", "[RETURN] pour voir les infos de debug" );          
        helpText3.getLocalTranslation().set(
                0,20,0);
        helpText3.setLightCombineMode(LightState.OFF);       
        rootNode.attachChild(helpText3);
        
        // un fond étoilé
        //buildSkyBox();
        
        // construit les murs invisibles pour la gestion des collisions
        buildInvisibleWall(8, 80, 80, 10);
        
        rootNode.attachChild(peopleNode);
        //peopleNode.setRenderState(ls);
        
        // maj de la scène
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();                      
    }

    /**
     * construit les murs invisibles pour la gestion des collisions sur les bords
     * @param height
     * @param length
     * @param width
     */
	private void buildInvisibleWall(float height, float length, float width, float thick)
	{
		Texture textureGrass = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/grass.jpg"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setTexture(textureGrass);
		
		// le "sol"
        Node node = new Node("floor");
        rootNode.attachChild( node );
        node.getLocalTranslation().set(0, 0, -5);        
        Box visualFloorBox = new Box( node.getName(), 
                new Vector3f(node.getLocalTranslation()), 
                length, width, thick);
        visualFloorBox.setRenderState(ts);
        node.attachChild( visualFloorBox );
        
        /*Texture all = TextureManager.loadTexture(
                getClass().getResource(
            "/ressources/grass.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        visualFloorBox.setTextureBuffer(0, all.);*/
        
        // le mur du haut
        boxUpWall = new Node("upWall");
        rootNode.attachChild( boxUpWall );  
        visualFloorBox = new Box( boxUpWall.getName(), 
                new Vector3f(), 
                length, thick, height);
        visualFloorBox.setRenderState(ts);
        boxUpWall.attachChild( visualFloorBox );
        BoundingBox bounding = new BoundingBox(
                new Vector3f(), 
                length, thick, height);
        boxUpWall.setModelBound(bounding);
        boxUpWall.updateModelBound();
        boxUpWall.setIsCollidable(true);
        boxUpWall.getLocalTranslation().set(0, 
                width + thick, height/2 + thick/2 - 5);
        
        // le mur du bas
        boxBottomWall = new Node("bottomWall" );
        rootNode.attachChild( boxBottomWall );    
        visualFloorBox = new Box( boxBottomWall.getName(), 
                new Vector3f(), 
                length, thick, height);
        visualFloorBox.setRenderState(ts);
        boxBottomWall.attachChild( visualFloorBox );
        bounding = new BoundingBox(
                new Vector3f(), 
                length, thick, height);
        boxBottomWall.setModelBound(bounding);
        boxBottomWall.updateModelBound();
        boxBottomWall.setIsCollidable(true);
        boxBottomWall.getLocalTranslation().set(0, 
                -(width + thick), height/2 + thick/2 - 5);   
        
        // le mur de gauche        
        boxLeftWall =  new Node("leftWall" );
        rootNode.attachChild( boxLeftWall );
        visualFloorBox = new Box( boxLeftWall.getName(), 
                new Vector3f(boxLeftWall.getLocalTranslation()), 
                thick, width, height);
        visualFloorBox.setRenderState(ts);
        boxLeftWall.attachChild( visualFloorBox );
        bounding = new BoundingBox(
                new Vector3f(), 
                thick, width, height);
        boxLeftWall.setModelBound(bounding);
        boxLeftWall.updateModelBound();
        boxLeftWall.setIsCollidable(true);
        boxLeftWall.getLocalTranslation().set(
        		-(width + thick), 
        		0, height/2 + thick/2 - 5);
        
        // le mur de droite        
        boxRightWall = new Node( "rightWall" );
        rootNode.attachChild( boxRightWall );
        visualFloorBox = new Box( boxRightWall.getName(), 
                new Vector3f(boxRightWall.getLocalTranslation()), 
                thick, width, height);
        visualFloorBox.setRenderState(ts);
        boxRightWall.attachChild( visualFloorBox );
        bounding = new BoundingBox(
                new Vector3f(), 
                thick, width, height);
        boxRightWall.setModelBound(bounding);
        boxRightWall.updateModelBound();
        boxRightWall.setIsCollidable(true);
        boxRightWall.getLocalTranslation().set(
                width + thick, 
                0, height/2 + thick/2 - 5);
        
        rootNode.updateCollisionTree();
        rootNode.updateRenderState();
        
        // et le plafond
        /*staticNode = physicsSpace.createStaticNode();
        rootNode.attachChild( staticNode );
        box = staticNode.createBox( "celling" );
        box.getLocalScale().set( thick, width, length );
        box.getLocalTranslation().set(
                0, 0, height+thick/2);*/
        
        /*visualFloorBox = new Box( box.getName(), 
                new Vector3f(box.getLocalTranslation()), 
                box.getLocalScale().x, 
                box.getLocalScale().y, 
                box.getLocalScale().z);
        staticNode.attachChild( visualFloorBox );*/
	}

    private void buildSkyBox() 
    {
        this.skyBox = new Skybox("skybox", 600, 600, 600);
        Quaternion rot = new Quaternion();
        Vector3f axis = new Vector3f(1, 0, 0).normalizeLocal();
        rot.fromAngleNormalAxis((float)Math.PI/4, axis);
        skyBox.getLocalRotation().multLocal(rot);
        
        Texture all = TextureManager.loadTexture(
                getClass().getResource(
            "/ressources/grass.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);        
 
        skyBox.setTexture(Skybox.NORTH, all);
        skyBox.setTexture(Skybox.WEST, all);
        skyBox.setTexture(Skybox.SOUTH, all);
        skyBox.setTexture(Skybox.EAST, all);
        skyBox.setTexture(Skybox.UP, all);
        skyBox.setTexture(Skybox.DOWN, all);
        skyBox.preloadTextures();
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
            @Override
            public void performAction(InputActionEvent evt)
            {
                showDebug = !showDebug;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RETURN,
                InputHandler.AXIS_NONE, false);              
        
        input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                gameOver = true;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE,
                InputHandler.AXIS_NONE, true);
        
        //input.addAction(new MousePick(getCamera(), rootNode, helpText3));
        input.addAction(new MouseInputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
            	 if( MouseInput.get().isButtonDown(0))
            	 {
                     /*Vector3f location = new Vector3f(cameraNode.getLocalTranslation());
                     location.z += mouseNode.getLocalTranslation().y;
                     location.x += mouseNode.getLocalTranslation().x;
                     ray = new Ray(location, getCamera().getDirection());*/
                     ray = new Ray(
                             getCamera().getLocation(),
                             getCamera().getDirection()); // camera direction is already normalized
            	 }
            }

        });
        
        /*// je joueur vire à gauche
        input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                player.addForce(new Vector3f(-playerSpeed, 0.0f, 0.0f));
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LEFT,
                InputHandler.AXIS_NONE, true);
        
        // le joueur vire à droite
        input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                player.addForce(new Vector3f(playerSpeed, 0.0f, 0.0f));
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RIGHT,
                InputHandler.AXIS_NONE, true);
        
        // le joueur accelère
        input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                player.addForce(new Vector3f(0.0f, playerSpeed, 0.0f));
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_UP,
                InputHandler.AXIS_NONE, true);
        
        // le joueur freine
        input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                player.addForce(new Vector3f(0.0f, -playerSpeed, 0.0f));
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_DOWN,
                InputHandler.AXIS_NONE, true);

        // le joueur viens de percuter quelque chose
        SyntheticButton collisionPlayerEventHandler = player
                .getCollisionEventHandler();
        input.addAction(new InputAction()
        {

            @Override
            public void performAction(InputActionEvent evt)
            {
                // si il a percuté un ennemi (ou ce dernier le percute)
                // et que le "countdown" est terminé (espèce de bouclier temporaire on peut dire)                
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                if((contactInfo.getNode1() instanceof SimpleEnnemy || 
                    contactInfo.getNode2() instanceof SimpleEnnemy) && shieldCountdown <= 0)
                {
                    player.decreaseLife(1.0f);
                    shieldCountdown = 1;
                }                
            }
        }, collisionPlayerEventHandler.getDeviceName(), collisionPlayerEventHandler
                .getIndex(), InputHandler.AXIS_NONE, false);
        
        // l'ennemy touche quelque chose... si ce n'est pas une rocket, ça ne lui fait rien :)
        /*SyntheticButton collisionEnnemyEventHandler = ennemy1
            .getCollisionEventHandler();
        input.addAction(new InputAction()
        {        
            @Override
            public void performAction(InputActionEvent evt)
            {               
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                if((contactInfo.getNode1() instanceof Rocket || 
                   contactInfo.getNode2() instanceof Rocket) && ennemyShieldCountdown <= 0)
                {
                    ennemy1.decreaseLife(1.0f);
                    ennemyShieldCountdown = 0.2f;           
                }                
            }
        }, collisionEnnemyEventHandler.getDeviceName(), collisionEnnemyEventHandler
                .getIndex(), InputHandler.AXIS_NONE, false);*/
        
        /*input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                if(rocketCountDown <= 0)
                {
                    // création d'un missile
                    final Rocket rocket = new Rocket(physicsSpace, complex);                
                    physicsSpace.addNode(rocket);
                    rootNode.attachChild(rocket);
                    rocketList.add(rocket);
                    rocket.setLocalTranslation(new Vector3f(
                            player.getLocalTranslation().x, 
                            player.getLocalTranslation().y+5, // juste devant le joeur
                            player.getLocalTranslation().z));  
                    rocket.addForce(new Vector3f(0.0f, 100.0f, 0.0f));
                    rocketCountDown = playerFireRate;
                    rootNode.updateGeometricState(0, true);
                    rootNode.updateRenderState();
                    SyntheticButton collisionRocketEventHandler = 
                        rocket.getCollisionEventHandler();
                    input.addAction(new InputAction()
                    {        
                        @Override
                        public void performAction(InputActionEvent evt)
                        {           
                            // détermine l'autre
                            final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                            Node other = null;
                            if(contactInfo.getNode1().equals(rocket))
                                other = contactInfo.getNode2();
                            else
                                other = contactInfo.getNode1();
                            if(!(other instanceof Rocket) && rocketList.contains(rocket))
                            {
                                rootNode.detachChild(rocket);
                                rocketList.remove(rocket);
                                rocket.clearForce();
                                rocket.clearDynamics();
                                rocket.setActive(false);
                            }
                        }
                    }, collisionRocketEventHandler.getDeviceName(), collisionRocketEventHandler
                            .getIndex(), InputHandler.AXIS_NONE, false);
                    
                }
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_SPACE,
                InputHandler.AXIS_NONE, true);*/
	}
    
    private float elapsedTime = 0;
    private float elapsedFrame = 0;
    private float logTime = 0;

    private Node boxUpWall;
    private Node boxBottomWall;
    private Node boxLeftWall;
    private Node boxRightWall;

    private float stateTime = 0;
    //private int numberPeople = 0;

	private AbsoluteMouse mouseNode;

	private Ray ray;

	private PickResults pickResults;
    
    @Override
    protected void stateUpdate(float tpf)
    {
        input.update(tpf); 
        stateTime += tpf;
        elapsedTime += tpf;
        logTime += tpf;
        
        if(logTime >= 10.0f)
        {
            StatLogger.GetInstance().LogPeople(peopleNode.getChildren());
            logTime = 0;
        }
        
        if(stateTime < 0.5f)
            return;        
        
        float fps = ((Float)(elapsedFrame/stateTime)).intValue();
        
        elapsedFrame = 0.0f;
        stateTime = 0.0f;
                
        /*pickResults.clear();
        pickResults.setCheckDistance(true);
        rootNode.findPick(ray,pickResults);
        
        if(pickResults.getNumber() > 0)
        {
        	Node source = pickResults.getPickData(0)
                    .getTargetMesh().getParentGeom().getParent().getParent();
        	if(source instanceof People)
        	{
        		People selected = (People)source;
        		helpText3.print("Selected => life="+selected.getLife()+
        				" speed="+selected.getSpeed()+" prolific="+selected.getProlific());
        	}
        }*/
        
        // maladie HAHAHA!
        if(peopleNode.getChildren().size() > 70 && fps < 30)
        {
            Random random = new Random();
            for(int cpt = 0; cpt < 10; cpt++)
            {
                ((People)peopleNode.getChild(random.nextInt(69))).setFeeling(PeopleFeeling.DEAD);
                StatLogger.GetInstance().LogDeath(DeathCause.disease);
            }
        }
        
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
        helpText1.print("nb individu : "+peopleNode.getChildren().size());
        helpText2.print("elapsed time : "+(int)elapsedTime/60+"min "+(int)elapsedTime%60+"sec");
        helpText3.print("");
        fpsText.print("fps : "+fps);                     
        
        super.stateUpdate(tpf);
    }
    
    
    @Override
    protected void onActivate()
    {
        handleActions();
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
        /*if(showDebug)
        {
            PhysicsDebugger.drawPhysics( physicsSpace, DisplaySystem.getDisplaySystem().getRenderer() );
        }*/
        super.stateRender(tpf);        
    }


}
