package game;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.scene.Text;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.CameraGameState;

public class MenuState extends CameraGameState
{
    
    private InputHandler input;
    
    private Text menuStartText;
    private Text menuQuitText;
    private Text menuPeopleText;
    private Text menuLogText;
    
    private Integer nbPeople = 50;
    public Integer getNbPeople()
    {
        return nbPeople;
    }
    
    private Boolean startGame = false;
    public Boolean isStartGame()
    {
        return startGame;
    }
    private Boolean quitGame = false;
    public Boolean isQuitGame()
    {
        return quitGame;
    }
    
    public MenuState(String name)
    {
        super(name);     
        
        
        menuStartText = Text.createDefaultTextLabel( "menuStartText", "" );          
        menuStartText.getLocalTranslation().set(
                40,DisplaySystem.getDisplaySystem().getHeight()/2+20,0);
        menuStartText.setLightCombineMode(LightState.OFF);
        rootNode.attachChild(menuStartText);                
        
        menuPeopleText = Text.createDefaultTextLabel( "menuNbPeopleText", "" );          
        menuPeopleText.getLocalTranslation().set(
                40,DisplaySystem.getDisplaySystem().getHeight()/2-20,0);
        menuPeopleText.setLightCombineMode(LightState.OFF);
        rootNode.attachChild(menuPeopleText);
        
        menuLogText = Text.createDefaultTextLabel( "menuLogText", "" );          
        menuLogText.getLocalTranslation().set(
                40,DisplaySystem.getDisplaySystem().getHeight()/2-40,0);
        menuLogText.setLightCombineMode(LightState.OFF);
        rootNode.attachChild(menuLogText);
        
        menuQuitText = Text.createDefaultTextLabel( "menuQuitText", "" );          
        menuQuitText.getLocalTranslation().set(
                40,DisplaySystem.getDisplaySystem().getHeight()/2-90,0);
        menuQuitText.setLightCombineMode(LightState.OFF);
        rootNode.attachChild(menuQuitText);
        
        input = new InputHandler();        
        
        // maj de la scène
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();
    }        

    @Override
    protected void stateUpdate(float tpf)
    {
        input.update(tpf);
        menuLogText.print("[L] log : "+(StatLogger.GetInstance().isEnable()?"True":"False"));
        menuPeopleText.print("[UP][DOWN] pour changer le nombre d'individu : "+nbPeople);
        menuStartText.print("[RETURN] to start a new game");        
        menuQuitText.print("[ESC] to quit");
        
        super.stateUpdate(tpf);
    }
    
    @Override
    protected void stateRender(float tpf)
    {
        DisplaySystem.getDisplaySystem().getRenderer().clearBuffers();
        super.stateRender(tpf);
    }

    private float changePeople = 0;
    
    @Override
    protected void onActivate()
    {
        startGame = false;
        quitGame = false;
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                if(!evt.getTriggerPressed())
                    startGame = true;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RETURN,
                InputHandler.AXIS_NONE, false);
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                if(!evt.getTriggerPressed())
                    quitGame = true;
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE,
                InputHandler.AXIS_NONE, false);
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                changePeople+=evt.getTime();
                if(changePeople >= 0.2f)
                {
                    nbPeople++;
                    changePeople = 0;
                }
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_UP,
                InputHandler.AXIS_NONE, true);
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                changePeople+=evt.getTime();
                if(changePeople >= 0.2f)
                {
                    nbPeople--;
                    changePeople = 0;
                }
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_DOWN,
                InputHandler.AXIS_NONE, true);
        super.onActivate();
        
        input.addAction(new InputAction()
        {
            public void performAction(InputActionEvent evt)
            {
                if(!evt.getTriggerPressed())
                    StatLogger.GetInstance().setEnable(!StatLogger.GetInstance().isEnable());
            }

        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_L,
                InputHandler.AXIS_NONE, false);
        super.onActivate();
    }
    
    @Override
    protected void onDeactivate()
    {
        startGame = false;
        quitGame = false;
        input.clearActions();
        super.onDeactivate();
    }

}
