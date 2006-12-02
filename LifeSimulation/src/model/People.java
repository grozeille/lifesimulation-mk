package model;

import java.io.IOException;

import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
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

    public People(Float[] chromosome) throws IOException
    {
        this(chromosome[0], chromosome[1], chromosome[2], 
             chromosome[3], chromosome[4], chromosome[5],  
             chromosome[6], chromosome[7], chromosome[8]);
    }
    
    public People(float life, float curious, float speed, float sensibility,
            float fear, float prolific, float charming, float defence,
            float canibal) throws IOException
    {        
        this.chromosome = new Float[9];
        this.chromosome[People.GENE_LIFE] = life;                
        this.chromosome[People.GENE_CURIOUS] = curious;
        this.chromosome[People.GENE_SPEED] = speed;
        this.chromosome[People.GENE_SENSIBILITY] = sensibility;
        this.chromosome[People.GENE_FEAR] = fear;
        this.chromosome[People.GENE_PROLIFIC] = prolific;
        this.chromosome[People.GENE_CHARMING] = charming;
        this.chromosome[People.GENE_DEFENCE] = defence;
        this.chromosome[People.GENE_CANIBAL] = canibal;
        
        buildModel();

        setFeeling(PeopleFeeling.BORN);

        updateColor();     

        input = new InputHandler();

        peopleController = new PeopleController(this);
        this.addController(peopleController);
    }
    
    private void buildModel() throws IOException
    {
        this.model = ModelManager.getInstance().loadModel("people");        
        this.model.setLocalScale(0.08f * (this.chromosome[People.GENE_LIFE] / 100f));
        this.attachChild(this.model);

        BillboardNode message = new BillboardNode("peopleMessage");
        messageBox = new Quad("messageBox", 5, 5);
        texturePassive = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-plain.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureLove = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-kiss.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureComeBack = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-surprise.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureBorn = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-angel.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureDead = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-devil-grin.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureHungry = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-grin.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureDefence = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-crying.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);        
        textureSick = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-sick.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        textureGestation = TextureManager.loadTexture(getClass().getResource(
                "/ressources/face-laughing.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR);
        

        ts = DisplaySystem.getDisplaySystem().getRenderer()
                .createTextureState();
        AlphaState as = DisplaySystem.getDisplaySystem().getRenderer()
                .createAlphaState();
        ts.setTexture(texturePassive);
        messageBox.setRenderState(ts);
        messageBox.setRenderState(as);
        as.setBlendEnabled(true);
        messageBox.setLightCombineMode(LightState.OFF);
        messageBox.getLocalTranslation().set(0.0f,
                10f * (this.model.getLocalScale().x / 0.04f), 0.0f);
        message.attachChild(messageBox);        
        this.attachChild(message);
        
        this.getLocalTranslation().set(0.0f, 0.0f,
                10f * (this.model.getLocalScale().x / 0.04f));
        
        this.lookAt(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f,
                0.0f));
    }

    public People() throws IOException
    {
        super();
        this.chromosome = new Float[]{10f,10f,10f,10f,10f,10f,10f,10f,10f};
        
        buildModel();

        setFeeling(PeopleFeeling.BORN);

        updateColor();       

        input = new InputHandler();

        peopleController = new PeopleController(this);
        this.addController(peopleController);
    }

    private TextureState ts;

    private Texture textureHungry;

    private Texture texturePassive;

    private Texture textureDefence;

    private Texture textureLove;

    private Texture textureComeBack;

    private Texture textureBorn;

    private Texture textureDead;
    
    private Texture textureSick;
    
    private Texture textureGestation;

    private PeopleController peopleController;

    private Node model;

    private Float[] chromosome = new Float[9];

    public static Integer GENE_LIFE = 0;

    public static Integer GENE_CURIOUS = 1;

    public static Integer GENE_SPEED = 2;

    public static Integer GENE_SENSIBILITY = 3;

    public static Integer GENE_FEAR = 4;

    public static Integer GENE_PROLIFIC = 5;

    public static Integer GENE_CANIBAL = 6;

    public static Integer GENE_CHARMING = 7;

    public static Integer GENE_DEFENCE = 8;

    public Float[] getChromosome()
    {
        return this.chromosome;
    }

    private PeopleFeeling feeling = PeopleFeeling.BORN;

    private People lovedPeople = null;

    private Quad messageBox;

    public PeopleFeeling getFeeling()
    {
        return feeling;
    }

    public Node getModel()
    {
        return model;
    }

    public void setFeeling(PeopleFeeling feeling)
    {
        this.feeling = feeling;
        if (this.feeling == PeopleFeeling.PASSIVE)
            ts.setTexture(texturePassive);
        else if (this.feeling == PeopleFeeling.LOVE)
            ts.setTexture(textureLove);
        else if (this.feeling == PeopleFeeling.TIRED)
            ts.setTexture(textureComeBack);
        else if (this.feeling == PeopleFeeling.BORN)
            ts.setTexture(textureBorn);
        else if (this.feeling == PeopleFeeling.DEAD)
            ts.setTexture(textureDead);
        else if (this.feeling == PeopleFeeling.HUNGRY)
            ts.setTexture(textureHungry);
        else if (this.feeling == PeopleFeeling.DEFENCE)
            ts.setTexture(textureDefence);
        else if (this.feeling == PeopleFeeling.SICK)
            ts.setTexture(textureSick);
        else if(this.feeling == PeopleFeeling.GESTATION)
            ts.setTexture(textureGestation);
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
        ColorRGBA color = new ColorRGBA();
        ColorRGBA yellow = new ColorRGBA(0.94f, 0.88f, 0.0f, 1.0f);
        
        color.r = (chromosome[People.GENE_LIFE] + chromosome[People.GENE_PROLIFIC])
            / 200f - chromosome[People.GENE_CANIBAL] / 200f;
        color.b = (chromosome[People.GENE_CHARMING] + chromosome[People.GENE_DEFENCE])
            / 200f - chromosome[People.GENE_CANIBAL] / 200f;
        color.g = (chromosome[People.GENE_SPEED]
                              + chromosome[People.GENE_FEAR] + chromosome[People.GENE_CURIOUS])
                              / 300f - chromosome[People.GENE_CANIBAL] / 200f;
        
        /*for(int cpt = 0; cpt < this.model.getChildren().size(); cpt++)
        {
            if(this.model.getChild(cpt) instanceof TriMesh)
            {
                TriMesh t = (TriMesh)this.model.getChild(cpt);
                t.setDefaultColor(color);
            }
        }*/
        
        MaterialState materialState;
        // 0 : trompe ?
        // 1 : les antennes
        // 2 : le corps
        // 3 : yeux ?
        materialState = (MaterialState) this.model.getChild(1)
            .getRenderState(RenderState.RS_MATERIAL);
        materialState.setDiffuse(new ColorRGBA(yellow));
        materialState.setAmbient(new ColorRGBA(yellow));
        
        materialState = (MaterialState) this.model.getChild(2)
            .getRenderState(RenderState.RS_MATERIAL);
        materialState.setDiffuse(new ColorRGBA(color));
        materialState.setAmbient(new ColorRGBA(color));
    
        this.updateRenderState();
    }
}
