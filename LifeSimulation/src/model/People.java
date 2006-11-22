package model;

import java.io.IOException;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.Vector3f;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class People extends Node
{
    private static final long serialVersionUID = -4433386920425680300L;    

    public InputHandler input;
    
    public People(float life, float curious, float speed, float sensibility,
            float fear, float prolific, float charming, float defence, float canibal) throws IOException
    {
        this();
        this.life = life;
        this.model.setLocalScale(0.08f * (life/100f));
        this.getLocalTranslation().set(
                0.0f, 0.0f, 10f*(this.model.getLocalScale().x/0.04f));
        this.messageBox.getLocalTranslation().set(
                0.0f, 10f*(this.model.getLocalScale().x/0.04f), 0.0f);
        this.curious = curious;
        this.speed = speed;
        this.sensibility = sensibility;
        this.fear = fear;
        this.prolific = prolific;
        this.charming = charming;
        this.defence = defence; 
        this.canibal = canibal;
        updateColor();
    }
    
    public People() throws IOException
    {
        super();
        this.model = ModelManager.getInstance().loadModel("people");
        //this.model.setLocalTranslation(this.getLocalTranslation());
        this.model.setLocalScale(0.04f);  
        BoundingSphere bounding = new BoundingSphere(100.0f, 
                new Vector3f(this.getLocalTranslation()));     
        this.model.setModelBound(bounding);
        this.model.updateModelBound();
        this.setIsCollidable(true);
        this.attachChild(this.model);
        
        BillboardNode message = new BillboardNode("peopleMessage");        
        messageBox = new Quad("messageBox", 5, 5);
        texturePassive = TextureManager.loadTexture(
                getClass().getResource(
            "/ressources/face-plain.png"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        textureLove = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-kiss.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureComeBack = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-surprise.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureBorn = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-angel.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureDead = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-devil-grin.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureHungry = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-grin.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureDefence = TextureManager.loadTexture(
                getClass().getResource(
                "/ressources/face-crying.png"),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        AlphaState as =  DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
        ts.setTexture(texturePassive);
        messageBox.setRenderState(ts);        
        messageBox.setRenderState(as);
        as.setBlendEnabled(true);
        messageBox.setLightCombineMode(LightState.OFF);
        messageBox.getLocalTranslation().set(0.0f, 5.0f, 0.0f);
        message.attachChild(messageBox);
        this.attachChild(message);
        
        setFeeling(PeopleFeeling.BORN);
        
        updateColor();
        
        /*Arrow arrow = new Arrow( "arrowDirection", 5,0.5f);
        arrow.lookAt(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f,
                1.0f));
        this.attachChild( arrow );
        arrow = new Arrow( "arrowUp", 5,0.5f);
        this.attachChild( arrow );*/       
        
        
        this.lookAt(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f,
                1.0f));

        input = new InputHandler(); 
            
        peopleController = new PeopleController(this);
        this.addController(peopleController);
        
        /*input.addAction(new InputAction()
        {
            @Override
            public void performAction(InputActionEvent evt)
            {
                feeling = PeopleFeeling.COMEBACK;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_NUMPAD8,
                InputHandler.AXIS_NONE, true);*/
    }
    
    private TextureState ts;
    
    private Texture textureHungry;
    
    private Texture texturePassive;
    
    private Texture textureDefence;
    
    private Texture textureLove;
    
    private Texture textureComeBack;
    
    private Texture textureBorn;
    
    private Texture textureDead;
    
    private PeopleController peopleController;

    private Node model;

    private Float life = 50.0f;

    private Float curious = 10.0f;

    private Float speed = 50.0f;

    private Float sensibility = 10.0f;

    private Float fear = 10.0f;

    private Float prolific = 50.0f;
    
    private Float canibal = 10.0f; 

    private Float charming = 10.0f;

    private Float defence = 10.0f;

    private PeopleFeeling feeling = PeopleFeeling.BORN;
    
    private People lovedPeople = null;

    private Quad messageBox; 
    
    public Float getCanibal()
    {
        return canibal;
    }
    
    public Float getCharming()
    {
        return charming;
    }

    public Float getCurious()
    {
        return curious;
    }

    public Float getDefence()
    {
        return defence;
    }

    public Float getFear()
    {
        return fear;
    }

    public PeopleFeeling getFeeling()
    {
        return feeling;
    }

    public Float getLife()
    {
        return life;
    }

    public Node getModel()
    {
        return model;
    }

    public Float getProlific()
    {
        return prolific;
    }

    public Float getSensibility()
    {
        return sensibility;
    }

    public Float getSpeed()
    {
        return speed;
    }


    public void setFeeling(PeopleFeeling feeling)
    {
        this.feeling = feeling;
        if(this.feeling == PeopleFeeling.PASSIVE)
        	ts.setTexture(texturePassive);
        else if(this.feeling == PeopleFeeling.LOVE)
        	ts.setTexture(textureLove);
        else if(this.feeling == PeopleFeeling.TIRED)
        	ts.setTexture(textureComeBack);
        else if(this.feeling == PeopleFeeling.BORN)
        	ts.setTexture(textureBorn);
        else if(this.feeling == PeopleFeeling.DEAD)
        	ts.setTexture(textureDead);
        else if(this.feeling == PeopleFeeling.HUNGRY)
            ts.setTexture(textureHungry);
        else if(this.feeling == PeopleFeeling.DEFENCE)
            ts.setTexture(textureDefence);
     	this.updateRenderState();
    }

    public People getLovedPeople()
    {
        return lovedPeople;
    }

    public void setLovedPeople(People lovedPeople)
    {
        this.lovedPeople = lovedPeople;
    }
    
    private void updateColor()
    {
        MaterialState materialState = (MaterialState) this.model.getChild(2).getRenderState(RenderState.RS_MATERIAL);
        materialState.getDiffuse().r = (life+prolific)/200f - canibal/200f;
        materialState.getDiffuse().g = (speed+fear+curious)/300f - canibal/200f;
        materialState.getDiffuse().b = (charming+defence)/200f - canibal/200f;
        
        materialState.getAmbient().r = (life+prolific)/200f - canibal/200f;
        materialState.getAmbient().g = (speed+fear+curious)/300f - canibal/200f;
        materialState.getAmbient().b = (speed+fear+curious)/300f - canibal/200f;
    }
}
